package br.com.controlei.infrastructure.ai;

import br.com.controlei.application.contracts.SpeechToTextClient;
import br.com.controlei.application.contracts.TextToSpeechClient;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Locale;

/**
 * Liga a voz do assistente a um provedor compativel com a API de audio da OpenAI (/audio/transcriptions e
 * /audio/speech), pelo Spring AI. So existe com {@code controlei.voice.enabled=true}, que por padrao segue a IA do
 * servidor (mesmo interruptor, chave e provedor; ver application.properties). Sem ela, as portas ficam sem
 * implementacao e a rota de voz responde 503, sem afetar o chat de texto.
 *
 * <p>Os modelos sao montados aqui, a mao, e nao pelo starter do Spring AI: nada se configura sozinho por existir no
 * classpath.
 *
 * <p>{@code controlei.voice.provider}: com {@code openrouter} (padrao, o provedor da IA), transcricao e fala vao por
 * RestClient no formato do OpenRouter, com {@code data_collection=deny} (o Spring AI nao deixa incluir esse campo, e a
 * transcricao dele manda multipart, que o OpenRouter nao aceita); com {@code openai}, as duas usam o Spring AI.
 */
@Configuration
@ConditionalOnProperty(name = "controlei.voice.enabled", havingValue = "true")
public class VoiceAiConfig {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final String apiKey;
    private final String baseUrl;
    private final boolean openRouter;

    public VoiceAiConfig(@Value("${controlei.voice.api-key:}") String apiKey,
                         @Value("${controlei.voice.base-url:https://openrouter.ai/api/v1}") String baseUrl,
                         @Value("${controlei.voice.provider:openrouter}") String provider) {
        if (apiKey == null || apiKey.isBlank()) {
            // Falha na subida, com mensagem clara, em vez de 401 na primeira pergunta
            throw new IllegalStateException("A voz segue a IA do servidor: CONTROLEI_AI_ENABLED=true exige CONTROLEI_AI_API_KEY");
        }
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.openRouter = "openrouter".equals(provider.trim().toLowerCase(Locale.ROOT));
    }

    /** Transcricao pelo Spring AI (multipart, formato da OpenAI). Usada com {@code controlei.voice.provider=openai}. */
    public OpenAiAudioTranscriptionModel voiceTranscriptionModel(String model) {
        return OpenAiAudioTranscriptionModel.builder()
                .options(OpenAiAudioTranscriptionOptions.builder()
                        .apiKey(apiKey)
                        .baseUrl(baseUrl)
                        .model(model)
                        .language("pt")
                        .temperature(0f)
                        .timeout(TIMEOUT)
                        .maxRetries(1)
                        .build())
                .build();
    }

    /** Fala pelo Spring AI (formato da OpenAI). Usada com {@code controlei.voice.provider=openai}. */
    public OpenAiAudioSpeechModel voiceSpeechModel(String model, String voice) {
        return OpenAiAudioSpeechModel.builder()
                .options(OpenAiAudioSpeechOptions.builder()
                        .apiKey(apiKey)
                        .baseUrl(baseUrl)
                        .model(model)
                        .voice(voice)
                        .responseFormat(OpenAiAudioSpeechOptions.AudioResponseFormat.MP3)
                        .timeout(TIMEOUT)
                        .maxRetries(1)
                        .build())
                .build();
    }

    @Bean
    public SpeechToTextClient speechToTextClient(ObjectMapper mapper,
                                                 @Value("${controlei.voice.stt-model:openai/whisper-large-v3-turbo}") String model) {
        return openRouter
                ? new OpenRouterSpeechToTextClient(mapper, apiKey, baseUrl, model)
                : new SpringAiSpeechToTextClient(voiceTranscriptionModel(model));
    }

    @Bean
    public TextToSpeechClient textToSpeechClient(
            ObjectMapper mapper,
            @Value("${controlei.voice.tts-model:openai/gpt-4o-mini-tts-2025-12-15}") String model,
            @Value("${controlei.voice.tts-voice:alloy}") String voice) {
        return openRouter
                ? new OpenRouterTextToSpeechClient(mapper, apiKey, baseUrl, model, voice)
                : new SpringAiTextToSpeechClient(voiceSpeechModel(model, voice));
    }
}
