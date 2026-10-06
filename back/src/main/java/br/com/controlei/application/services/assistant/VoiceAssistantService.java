package br.com.controlei.application.services.assistant;

import br.com.controlei.application.contracts.SpeechToTextClient;
import br.com.controlei.application.contracts.TextToSpeechClient;
import br.com.controlei.application.contracts.VoiceSettingsRepository;
import br.com.controlei.application.exceptions.BusinessException;
import br.com.controlei.application.exceptions.ForbiddenException;
import br.com.controlei.application.exceptions.VoiceException;
import br.com.controlei.application.services.AuditLogService;
import br.com.controlei.application.services.AuthorizationService;
import br.com.controlei.application.services.assistant.PendingActions.Prepared;
import br.com.controlei.application.services.receipt.AiQuota;
import br.com.controlei.domain.contracts.repositories.AssistantSettingsRepositoryPort;
import br.com.controlei.domain.models.enums.AuditAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Voz do assistente: audio -> transcricao -> o MESMO {@link AssistantService#ask} do chat de texto -> sintese da
 * resposta. A voz so muda como a pergunta entra e como a resposta sai; isolamento por familia, papeis, ferramentas,
 * limite de rodadas, cota de perguntas e a confirmacao de escrita continuam todos no AssistantService.
 *
 * <p>Regras proprias da voz:
 * <ul>
 *   <li>interruptor proprio por familia, desligado por padrao, e so funciona com o assistente tambem ligado;</li>
 *   <li>cota diaria propria e disjuntor proprio ({@value #FAILURES_TO_OPEN} falhas seguidas do provedor de voz);</li>
 *   <li>a transcricao e entrada nao confiavel: entra como pergunta comum, nunca como instrucao do sistema;</li>
 *   <li>a falha da sintese nunca derruba a resposta: volta o texto com audio nulo;</li>
 *   <li>nada de audio ou transcricao em disco, banco ou log (no log: tamanho, duracao e resultado);</li>
 *   <li>sem transacao: as chamadas ao provedor nunca seguram conexao do banco.</li>
 * </ul>
 */
@Service
public class VoiceAssistantService {

    private static final Logger log = LoggerFactory.getLogger(VoiceAssistantService.class);

    static final int FAILURES_TO_OPEN = 3;
    static final Duration OPEN_FOR = Duration.ofSeconds(60);
    /** Resposta falada curta: o texto completo continua na tela. */
    static final int MAX_SPOKEN = 600;

    public record AudioReply(String mimeType, String base64) {}

    public record VoiceAnswer(String transcript, String answer, boolean ai, List<Prepared> actions, AudioReply audio) {}

    /**
     * Estado para a tela: interruptor da familia, se quem pergunta pode muda-lo, se a voz existe neste servidor e se o
     * assistente (pre-requisito) esta ligado.
     */
    public record VoiceSettings(boolean enabled, boolean canManage, boolean voiceAvailable, boolean assistantEnabled) {}

    private final ObjectProvider<SpeechToTextClient> stt;
    private final ObjectProvider<TextToSpeechClient> tts;
    private final AssistantService assistant;
    private final VoiceSettingsRepository voiceSettings;
    private final AssistantSettingsRepositoryPort assistantSettings;
    private final AuthorizationService authorization;
    private final AuditLogService audit;
    private final AiQuota quota;
    private final Clock clock;
    private final AtomicInteger consecutiveFailures = new AtomicInteger();
    private final AtomicReference<Instant> openUntil = new AtomicReference<>(Instant.MIN);

    @Autowired
    public VoiceAssistantService(ObjectProvider<SpeechToTextClient> stt, ObjectProvider<TextToSpeechClient> tts,
                                 AssistantService assistant, VoiceSettingsRepository voiceSettings,
                                 AssistantSettingsRepositoryPort assistantSettings, AuthorizationService authorization,
                                 AuditLogService audit,
                                 @Value("${controlei.ai.voice-daily-limit-per-family:20}") int dailyLimit) {
        this(stt, tts, assistant, voiceSettings, assistantSettings, authorization, audit, new AiQuota(dailyLimit),
                Clock.systemUTC());
    }

    VoiceAssistantService(ObjectProvider<SpeechToTextClient> stt, ObjectProvider<TextToSpeechClient> tts,
                          AssistantService assistant, VoiceSettingsRepository voiceSettings,
                          AssistantSettingsRepositoryPort assistantSettings, AuthorizationService authorization,
                          AuditLogService audit, AiQuota quota, Clock clock) {
        this.stt = stt;
        this.tts = tts;
        this.assistant = assistant;
        this.voiceSettings = voiceSettings;
        this.assistantSettings = assistantSettings;
        this.authorization = authorization;
        this.audit = audit;
        this.quota = quota;
        this.clock = clock;
    }

    public VoiceAnswer ask(AudioClip clip, List<AssistantService.Turn> history) {
        SpeechToTextClient transcriber = stt.getIfAvailable();
        if (transcriber == null) {
            throw VoiceException.unavailable("A voz não está configurada neste servidor. Use o chat de texto.");
        }
        UUID familyId = authorization.currentFamilyId();
        if (!assistantSettings.isEnabled(familyId) || !voiceSettings.isEnabled(familyId)) {
            throw new ForbiddenException("A voz do assistente está desativada para a sua família.");
        }
        if (circuitOpen()) {
            throw VoiceException.unavailable("A voz está instável agora. Tente de novo em instantes ou digite a pergunta.");
        }
        if (!quota.tryAcquire(familyId)) {
            throw VoiceException.quotaExceeded("A cota de perguntas por voz da sua família acabou hoje. O chat de texto continua disponível.");
        }

        String transcript;
        try {
            transcript = AssistantService.sanitize(transcriber.transcribe(clip.bytes(), clip.mimeType(), clip.filename()),
                    AssistantService.MAX_QUESTION);
            providerOk();
        } catch (RuntimeException e) {
            providerFailed();
            logOutcome(clip, "falha na transcricao (" + e.getClass().getSimpleName() + ")");
            throw VoiceException.notUnderstood("Não consegui transcrever o áudio. Tente de novo ou digite a pergunta.");
        }
        if (transcript.isBlank()) {
            logOutcome(clip, "transcricao vazia");
            throw VoiceException.notUnderstood("Não ouvi nenhuma pergunta no áudio. Tente falar mais perto do microfone.");
        }

        // O mesmo caminho do texto: ferramentas, confirmacao de escrita, cota e disjuntor do assistente
        AssistantService.Answer answer = assistant.ask(transcript, history);

        AudioReply audio = speak(answer.answer());
        logOutcome(clip, audio != null ? "ok" : "ok sem audio");
        return new VoiceAnswer(transcript, answer.answer(), answer.ai(), answer.actions(), audio);
    }

    /** A sintese e opcional: qualquer falha devolve null e a resposta segue em texto. */
    private AudioReply speak(String answer) {
        TextToSpeechClient synthesizer = tts.getIfAvailable();
        String text = speakable(answer);
        if (synthesizer == null || text.isBlank() || circuitOpen()) {
            return null;
        }
        try {
            byte[] mp3 = synthesizer.synthesize(text);
            providerOk();
            return mp3 == null || mp3.length == 0 ? null : new AudioReply("audio/mpeg", Base64.getEncoder().encodeToString(mp3));
        } catch (RuntimeException e) {
            providerFailed();
            log.warn("voz: sintese falhou ({}); resposta segue so em texto", e.getClass().getSimpleName());
            return null;
        }
    }

    public VoiceSettings settings() {
        UUID familyId = authorization.currentFamilyId();
        return new VoiceSettings(voiceSettings.isEnabled(familyId), authorization.isResponsible(),
                stt.getIfAvailable() != null, assistantSettings.isEnabled(familyId));
    }

    /**
     * Liga ou desliga a voz da familia. So o responsavel; para ligar, ele declara ciencia de que o audio sai do servidor
     * para um provedor externo de transcricao e voz. Fica no log de auditoria.
     */
    public VoiceSettings updateSettings(boolean enabled, boolean acknowledged) {
        authorization.requireResponsible();
        if (enabled && !acknowledged) {
            throw new BusinessException("Para ativar a voz, confirme que está ciente de que o áudio será enviado a um provedor externo");
        }
        UUID familyId = authorization.currentFamilyId();
        UUID userId = authorization.currentUserId();
        voiceSettings.setEnabled(familyId, enabled, userId);
        audit.logAction(familyId, userId, "VOICE_SETTINGS", familyId, AuditAction.UPDATE, null,
                enabled ? "voz do assistente ativada" : "voz do assistente desativada", null, null);
        return settings();
    }

    /** Texto para falar: sem markdown e com ate ~{@value #MAX_SPOKEN} caracteres, cortado no fim de uma frase. */
    static String speakable(String text) {
        if (text == null) {
            return "";
        }
        String plain = text
                .replaceAll("\\[([^\\]]*)]\\([^)]*\\)", "$1")          // [texto](link) -> texto
                .replaceAll("(?m)^\\s{0,3}#{1,6}\\s*", "")             // titulos
                .replaceAll("(?m)^\\s*(?:[-*+]|\\d+[.)])\\s+", "")     // marcadores de lista
                .replaceAll("[*_`~>|]", "")                             // enfase, codigo, citacao, tabela
                .replaceAll("\\s+", " ")
                .strip();
        if (plain.length() <= MAX_SPOKEN) {
            return plain;
        }
        String cut = plain.substring(0, MAX_SPOKEN);
        int end = Math.max(cut.lastIndexOf(". "), Math.max(cut.lastIndexOf("! "), cut.lastIndexOf("? ")));
        if (end >= MAX_SPOKEN / 2) {
            return cut.substring(0, end + 1);
        }
        int space = cut.lastIndexOf(' ');
        return (space > 0 ? cut.substring(0, space) : cut) + "...";
    }

    private boolean circuitOpen() {
        return clock.instant().isBefore(openUntil.get());
    }

    private void providerOk() {
        consecutiveFailures.set(0);
    }

    private void providerFailed() {
        if (consecutiveFailures.incrementAndGet() >= FAILURES_TO_OPEN) {
            openUntil.set(clock.instant().plus(OPEN_FOR));
            consecutiveFailures.set(0);
        }
    }

    /** So metadados: o audio e a transcricao nunca vao para o log. */
    private static void logOutcome(AudioClip clip, String outcome) {
        log.info("voz: {} bytes, {}, duracao {} ms, resultado: {}", clip.bytes().length, clip.mimeType(),
                clip.durationMs() == null ? "?" : clip.durationMs(), outcome);
    }
}
