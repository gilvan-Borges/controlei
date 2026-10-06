package br.com.controlei.infrastructure.ai;

import br.com.controlei.application.contracts.SpeechToTextClient;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Transcricao pelo OpenRouter. O /audio/transcriptions dele NAO e o multipart da OpenAI: recebe JSON com o audio em
 * base64 ({@code input_audio: {data, format}}) e devolve {@code {text}}. Por isso este adapter usa RestClient direto,
 * e nao o OpenAiAudioTranscriptionModel do Spring AI (que manda multipart). O audio so passa pela memoria.
 */
public class OpenRouterSpeechToTextClient implements SpeechToTextClient {

    private final RestClient http;
    private final ObjectMapper mapper;
    private final String model;

    public OpenRouterSpeechToTextClient(ObjectMapper mapper, String apiKey, String baseUrl, String model) {
        var factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(Duration.ofSeconds(30));
        this.http = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.mapper = mapper;
        this.model = model;
    }

    @Override
    public String transcribe(byte[] audio, String mimeType, String filename) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("input_audio", Map.of("data", Base64.getEncoder().encodeToString(audio), "format", format(filename)));
        body.put("language", "pt");
        body.put("temperature", 0);
        String response = http.post()
                .uri("/audio/transcriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(body))
                .retrieve()
                .body(String.class);
        JsonNode text = response == null ? null : mapper.readTree(response).path("text");
        return text == null || text.isMissingNode() || text.isNull() ? "" : text.asString();
    }

    /** O formato e a extensao do nome sintetico (webm, ogg, mp3, wav, m4a), que ja passou pela validacao. */
    static String format(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "webm" : filename.substring(dot + 1);
    }
}
