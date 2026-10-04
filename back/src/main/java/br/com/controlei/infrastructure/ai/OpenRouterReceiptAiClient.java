package br.com.controlei.infrastructure.ai;

import br.com.controlei.domain.contracts.ai.ReceiptAiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Cliente do OpenRouter para ler comprovantes. So existe quando {@code controlei.ai.enabled=true}.
 *
 * <p>Privacidade: o comprovante sai do servidor, por isso a IA vem desligada por padrao, o pedido proibe que
 * o provedor guarde ou treine com os dados ({@code data_collection=deny}) e nada do conteudo vai para o log.
 */
@Component
@ConditionalOnProperty(name = "controlei.ai.enabled", havingValue = "true")
public class OpenRouterReceiptAiClient implements ReceiptAiClient {

    private final RestClient http;
    private final String model;

    public OpenRouterReceiptAiClient(
            @Value("${controlei.ai.api-key}") String apiKey,
            @Value("${controlei.ai.model:google/gemini-2.5-flash}") String model,
            @Value("${controlei.ai.base-url:https://openrouter.ai/api/v1}") String baseUrl) {
        var factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(Duration.ofSeconds(30));
        this.http = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.model = model;
    }

    @Override
    public String completeJson(String systemPrompt, String untrustedText, ReceiptImage image) {
        List<Object> userContent = new ArrayList<>();
        if (untrustedText != null) {
            userContent.add(Map.of("type", "text", "text", untrustedText));
        }
        if (image != null) {
            String dataUri = "data:" + image.mimeType() + ";base64,"
                    + Base64.getEncoder().encodeToString(image.bytes());
            userContent.add(Map.of("type", "image_url", "image_url", Map.of("url", dataUri)));
        }
        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", 0,
                "max_tokens", 800,
                "response_format", Map.of("type", "json_object"),
                "provider", Map.of("data_collection", "deny"),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userContent)));

        JsonNode response = http.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
        if (response == null) {
            throw new IllegalStateException("resposta vazia do provedor de IA");
        }
        return response.path("choices").path(0).path("message").path("content").asString();
    }
}
