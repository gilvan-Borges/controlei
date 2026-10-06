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

import java.time.Duration;

/**
 * Liga a voz do assistente a um provedor compativel com a API de audio da OpenAI (/audio/transcriptions e
 * /audio/speech), pelo Spring AI. So existe com {@code controlei.voice.enabled=true}; sem ela, as portas ficam sem
 * implementacao e a rota de voz responde 503, sem afetar o chat de texto.
 *
 * <p>Os modelos sao montados aqui, a mao, e nao pelo starter do Spring AI: nada se configura sozinho por existir no
 * classpath, e a chave fica restrita a voz (o chat de texto usa outro provedor e outra chave).
 */
@Configuration
@ConditionalOnProperty(name = "controlei.voice.enabled", havingValue = "true")
public class VoiceAiConfig {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final String apiKey;
    private final String baseUrl;

    public VoiceAiConfig(@Value("${controlei.voice.api-key:}") String apiKey,
                         @Value("${controlei.voice.base-url:https://api.openai.com/v1}") String baseUrl) {
        if (apiKey == null || apiKey.isBlank()) {
            // Falha na subida, com mensagem clara, em vez de 401 na primeira pergunta
            throw new IllegalStateException("CONTROLEI_VOICE_ENABLED=true exige CONTROLEI_VOICE_API_KEY");
        }
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    @Bean
    public OpenAiAudioTranscriptionModel voiceTranscriptionModel(
            @Value("${controlei.voice.stt-model:whisper-1}") String model) {
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

    @Bean
    public OpenAiAudioSpeechModel voiceSpeechModel(
            @Value("${controlei.voice.tts-model:tts-1}") String model,
            @Value("${controlei.voice.tts-voice:alloy}") String voice) {
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
    public SpeechToTextClient speechToTextClient(OpenAiAudioTranscriptionModel voiceTranscriptionModel) {
        return new SpringAiSpeechToTextClient(voiceTranscriptionModel);
    }

    @Bean
    public TextToSpeechClient textToSpeechClient(OpenAiAudioSpeechModel voiceSpeechModel) {
        return new SpringAiTextToSpeechClient(voiceSpeechModel);
    }
}
