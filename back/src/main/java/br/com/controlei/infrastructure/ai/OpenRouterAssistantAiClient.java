package br.com.controlei.infrastructure.ai;

import br.com.controlei.domain.contracts.ai.AssistantAiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Cliente do OpenRouter para o assistente de ajuda. So existe com {@code controlei.ai.enabled=true}.
 * Envia apenas a pergunta e a base de conhecimento publica do app: nenhum dado financeiro da familia sai daqui.
 */
@Component
@ConditionalOnProperty(name = "controlei.ai.enabled", havingValue = "true")
public class OpenRouterAssistantAiClient implements AssistantAiClient {

    private final RestClient http;
    private final String model;

    public OpenRouterAssistantAiClient(
            @Value("${controlei.ai.api-key}") String apiKey,
            @Value("${controlei.ai.assistant-model:${controlei.ai.model:google/gemini-2.5-flash}}") String model,
            @Value("${controlei.ai.base-url:https://openrouter.ai/api/v1}") String baseUrl) {
        var factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(Duration.ofSeconds(20));
        this.http = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.model = model;
    }

    @Override
    public String answer(String systemPrompt, String question) {
        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", 0.2,
                "max_tokens", 400,
                "provider", Map.of("data_collection", "deny"),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", question)));
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
