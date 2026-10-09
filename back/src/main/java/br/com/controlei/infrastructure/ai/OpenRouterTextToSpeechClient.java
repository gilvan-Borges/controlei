package br.com.controlei.infrastructure.ai;

import br.com.controlei.application.contracts.TextToSpeechClient;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sintese da resposta pelo OpenRouter (/audio/speech, MP3). RestClient direto, e nao o OpenAiAudioSpeechModel do
 * Spring AI, porque o pedido precisa levar {@code provider: {data_collection: "deny"}} (o provedor nao guarda nem treina
 * com o conteudo), como o chat e os comprovantes, e o modelo do Spring AI nao deixa incluir esse campo.
 */
public class OpenRouterTextToSpeechClient implements TextToSpeechClient {

    private final RestClient http;
    private final ObjectMapper mapper;
    private final String model;
    private final String voice;

    public OpenRouterTextToSpeechClient(ObjectMapper mapper, String apiKey, String baseUrl, String model, String voice) {
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
        this.voice = voice;
    }

    @Override
    public byte[] synthesize(String text) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("input", text);
        body.put("voice", voice);
        body.put("response_format", "mp3");
        body.put("provider", Map.of("data_collection", "deny"));
        return http.post()
                .uri("/audio/speech")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.valueOf("audio/mpeg"), MediaType.APPLICATION_OCTET_STREAM)
                .body(mapper.writeValueAsString(body))
                .retrieve()
                .body(byte[].class);
    }
}
