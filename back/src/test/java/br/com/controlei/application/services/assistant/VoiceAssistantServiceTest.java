package br.com.controlei.application.services.assistant;

import br.com.controlei.application.contracts.SpeechToTextClient;
import br.com.controlei.application.contracts.TextToSpeechClient;
import br.com.controlei.application.contracts.VoiceSettingsRepository;
import br.com.controlei.application.exceptions.BusinessException;
import br.com.controlei.application.exceptions.ForbiddenException;
import br.com.controlei.application.exceptions.VoiceException;
import br.com.controlei.application.services.AuditLogService;
import br.com.controlei.application.services.AuthorizationService;
import br.com.controlei.application.services.receipt.AiQuota;
import br.com.controlei.domain.contracts.ai.AssistantAiClient;
import br.com.controlei.domain.contracts.ai.AssistantAiClient.Completion;
import br.com.controlei.domain.contracts.ai.AssistantAiClient.ToolCall;
import br.com.controlei.domain.contracts.repositories.AssistantSettingsRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Voz sobre o AssistantService REAL (com modelo e ferramenta falsos): a voz so troca a entrada e a saida, entao o que
 * vale para o texto (inclusive "escrita so prepara") tem de valer aqui sem nenhum codigo novo.
 */
class VoiceAssistantServiceTest {

    /** Cabecalho WebM minimo (EBML + DocType "webm"), o suficiente para passar pelos magic bytes. */
    static final byte[] WEBM = {0x1A, 0x45, (byte) 0xDF, (byte) 0xA3, (byte) 0x9F, 0x42, (byte) 0x82, (byte) 0x84,
            'w', 'e', 'b', 'm', 0x42, (byte) 0x87, (byte) 0x81, 0x04};

    private final UUID familyId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private AuthorizationService authorization;
    private AssistantAiClient ai;
    private AssistantToolbox toolbox;
    private VoiceSettingsRepository voiceSettings;
    private AssistantSettingsRepositoryPort assistantSettings;
    private FakeStt stt;
    private FakeTts tts;
    private PendingActions pending;
    private AtomicInteger executions;
    private MutableClock clock;

    /** Transcricao fixa ou falha, conforme o teste. */
    static class FakeStt implements SpeechToTextClient {
        String text = "quanto gastei este mes?";
        RuntimeException failure;
        int calls;

        @Override
        public String transcribe(byte[] audio, String mimeType, String filename) {
            calls++;
            if (failure != null) {
                throw failure;
            }
            return text;
        }
    }

    static class FakeTts implements TextToSpeechClient {
        RuntimeException failure;
        String lastText;
        int calls;

        @Override
        public byte[] synthesize(String text) {
            calls++;
            lastText = text;
            if (failure != null) {
                throw failure;
            }
            return new byte[]{'I', 'D', '3', 1};
        }
    }

    static class MutableClock extends Clock {
        Instant now = Instant.parse("2026-10-06T12:00:00Z");

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    @BeforeEach
    void setUp() {
        authorization = mock(AuthorizationService.class);
        when(authorization.currentFamilyId()).thenReturn(familyId);
        when(authorization.currentUserId()).thenReturn(userId);
        when(authorization.isResponsible()).thenReturn(true);
        ai = mock(AssistantAiClient.class);
        when(ai.complete(anyList(), anyList())).thenReturn(new Completion("Você gastou R$ 1.200,00 este mês.", List.of()));
        executions = new AtomicInteger();
        // Ferramenta de escrita falsa: so conta quantas vezes a acao REALMENTE rodou
        AssistantTool create = new AssistantTool("create_transaction", "lanca", "{\"type\":\"object\"}", true,
                args -> AssistantTool.Result.pending("Lançar despesa de R$ 50,00 em Alimentação",
                        () -> "lancado " + executions.incrementAndGet()));
        toolbox = mock(AssistantToolbox.class);
        when(toolbox.all()).thenReturn(List.of(create));
        when(toolbox.find("create_transaction")).thenReturn(create);
        voiceSettings = mock(VoiceSettingsRepository.class);
        when(voiceSettings.isEnabled(familyId)).thenReturn(true);
        assistantSettings = mock(AssistantSettingsRepositoryPort.class);
        when(assistantSettings.isEnabled(familyId)).thenReturn(true);
        stt = new FakeStt();
        tts = new FakeTts();
        pending = new PendingActions();
        clock = new MutableClock();
    }

    @SuppressWarnings("unchecked")
    private <T> ObjectProvider<T> providerOf(T value) {
        ObjectProvider<T> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(value);
        return provider;
    }

    private AssistantService assistant() {
        return new AssistantService(providerOf(ai), toolbox, pending, authorization, mock(AuditLogService.class),
                assistantSettings, new ObjectMapper(), 100);
    }

    private VoiceAssistantService service(int dailyLimit) {
        return new VoiceAssistantService(providerOf(stt), providerOf(tts), assistant(), voiceSettings, assistantSettings,
                authorization, mock(AuditLogService.class), new AiQuota(dailyLimit), clock);
    }

    private static AudioClip clip() {
        return AudioClip.of(WEBM, "audio/webm;codecs=opus", 3_000L);
    }

    @Test
    void happyPathTranscribesAnswersWithTheSameAssistantAndSpeaks() {
        var answer = service(20).ask(clip(), List.of());

        assertEquals("quanto gastei este mes?", answer.transcript());
        assertEquals("Você gastou R$ 1.200,00 este mês.", answer.answer());
        assertTrue(answer.ai());
        assertNotNull(answer.audio());
        assertEquals("audio/mpeg", answer.audio().mimeType());
        assertEquals(4, Base64.getDecoder().decode(answer.audio().base64()).length);
        assertEquals("Você gastou R$ 1.200,00 este mês.", tts.lastText);
    }

    @Test
    void emptyTranscriptionIs422AndNeverReachesTheAssistant() {
        stt.text = "   ";

        var e = assertThrows(VoiceException.class, () -> service(20).ask(clip(), List.of()));

        assertEquals(422, e.getStatus());
        verify(ai, never()).complete(anyList(), anyList());
    }

    @Test
    void transcriptionFailureIs422() {
        stt.failure = new IllegalStateException("provedor fora");

        assertEquals(422, assertThrows(VoiceException.class, () -> service(20).ask(clip(), List.of())).getStatus());
    }

    @Test
    void speechFailureStillReturnsTheTextAnswer() {
        tts.failure = new IllegalStateException("tts fora");

        var answer = service(20).ask(clip(), List.of());

        assertEquals("Você gastou R$ 1.200,00 este mês.", answer.answer());
        assertNull(answer.audio());
    }

    @Test
    void dailyQuotaIsPerFamilyAndAnswers429() {
        var service = service(1);
        service.ask(clip(), List.of());

        var e = assertThrows(VoiceException.class, () -> service.ask(clip(), List.of()));

        assertEquals(429, e.getStatus());
        assertEquals(1, stt.calls);
    }

    @Test
    void circuitOpensAfterThreeProviderFailuresAndClosesAfterAMinute() {
        var service = service(20);
        stt.failure = new IllegalStateException("fora");
        for (int i = 0; i < VoiceAssistantService.FAILURES_TO_OPEN; i++) {
            assertEquals(422, assertThrows(VoiceException.class, () -> service.ask(clip(), List.of())).getStatus());
        }

        var open = assertThrows(VoiceException.class, () -> service.ask(clip(), List.of()));
        assertEquals(503, open.getStatus());
        assertEquals(VoiceAssistantService.FAILURES_TO_OPEN, stt.calls); // aberto: nem chama o provedor

        stt.failure = null;
        clock.now = clock.now.plus(VoiceAssistantService.OPEN_FOR).plusSeconds(1);
        assertEquals("quanto gastei este mes?", service.ask(clip(), List.of()).transcript());
    }

    @Test
    void speechFailuresNeverBlockTranscription() {
        var service = service(20);
        tts.failure = new IllegalStateException("tts fora");
        for (int i = 0; i < VoiceAssistantService.FAILURES_TO_OPEN; i++) {
            assertNull(service.ask(clip(), List.of()).audio());
        }

        // Disjuntor da fala aberto: a pergunta seguinte ainda transcreve e responde, so que em texto
        var answer = service.ask(clip(), List.of());

        assertEquals("quanto gastei este mes?", answer.transcript());
        assertEquals("Você gastou R$ 1.200,00 este mês.", answer.answer());
        assertNull(answer.audio());
        assertEquals(VoiceAssistantService.FAILURES_TO_OPEN + 1, stt.calls);
        assertEquals(VoiceAssistantService.FAILURES_TO_OPEN, tts.calls, "com o disjuntor da fala aberto, nem tenta sintetizar");
    }

    @Test
    void providerFailureInTranscriptionGivesTheQuotaBack() {
        var service = service(1);
        stt.failure = new IllegalStateException("provedor fora");
        assertEquals(422, assertThrows(VoiceException.class, () -> service.ask(clip(), List.of())).getStatus());

        stt.failure = null;
        assertEquals("quanto gastei este mes?", service.ask(clip(), List.of()).transcript());
        assertEquals(429, assertThrows(VoiceException.class, () -> service.ask(clip(), List.of())).getStatus());
    }

    @Test
    void emptyTranscriptionStillCountsInTheQuota() {
        var service = service(1);
        stt.text = "";
        assertEquals(422, assertThrows(VoiceException.class, () -> service.ask(clip(), List.of())).getStatus());

        stt.text = "quanto gastei este mes?";
        assertEquals(429, assertThrows(VoiceException.class, () -> service.ask(clip(), List.of())).getStatus());
    }

    @Test
    void spokenWriteCommandOnlyPreparesTheActionLikeText() {
        stt.text = "gastei 50 no mercado";
        when(ai.complete(anyList(), anyList())).thenReturn(new Completion("Preparei o lançamento.",
                List.of(new ToolCall("c1", "create_transaction", "{\"amount\":50}"))));

        var answer = service(20).ask(clip(), List.of());

        assertEquals(1, answer.actions().size());
        assertEquals("Lançar despesa de R$ 50,00 em Alimentação", answer.actions().get(0).summary());
        assertEquals(0, executions.get(), "a voz nao pode executar nada sozinha");

        // So o clique de confirmacao (o mesmo endpoint do texto) executa, e uma vez so
        assertEquals("lancado 1", assistant().confirm(answer.actions().get(0).id()));
        assertEquals(1, executions.get());
    }

    @Test
    void voiceNeedsBothSwitchesOfTheFamily() {
        when(assistantSettings.isEnabled(familyId)).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> service(20).ask(clip(), List.of()));

        when(assistantSettings.isEnabled(familyId)).thenReturn(true);
        when(voiceSettings.isEnabled(familyId)).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> service(20).ask(clip(), List.of()));
        assertEquals(0, stt.calls);
    }

    @Test
    void withoutVoiceConfiguredOnTheServerAnswers503() {
        var service = new VoiceAssistantService(providerOf(null), providerOf(null), assistant(), voiceSettings,
                assistantSettings, authorization, mock(AuditLogService.class), new AiQuota(20), clock);

        assertEquals(503, assertThrows(VoiceException.class, () -> service.ask(clip(), List.of())).getStatus());
        assertFalse(service.settings().voiceAvailable());
    }

    @Test
    void turningOnRequiresTheResponsibleAndTheAcknowledgement() {
        var service = service(20);

        assertThrows(BusinessException.class, () -> service.updateSettings(true, false));
        verify(voiceSettings, never()).setEnabled(any(), anyBoolean(), any());

        service.updateSettings(true, true);
        verify(voiceSettings).setEnabled(familyId, true, userId);
    }

    @Test
    void speakableDropsMarkdownAndCutsAtASentence() {
        assertEquals("Total: R$ 10 em Alimentação", VoiceAssistantService.speakable("**Total:** R$ 10 em [Alimentação](/x)"));
        assertEquals("Itens um dois", VoiceAssistantService.speakable("Itens\n- um\n- dois"));

        String longText = "Frase curta. ".repeat(80);
        String spoken = VoiceAssistantService.speakable(longText);
        assertTrue(spoken.length() <= VoiceAssistantService.MAX_SPOKEN);
        assertTrue(spoken.endsWith("."));
    }
}
