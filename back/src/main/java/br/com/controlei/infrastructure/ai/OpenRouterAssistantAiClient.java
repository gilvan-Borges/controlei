package br.com.controlei.infrastructure.ai;

import br.com.controlei.domain.contracts.ai.AssistantAiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cliente do OpenRouter para o assistente (formato chat completions com ferramentas). So existe com
 * {@code controlei.ai.enabled=true}. O pedido proibe o provedor de guardar ou treinar com os dados
 * ({@code data_collection=deny}) e nada do conteudo vai para o log.
 */
@Component
@ConditionalOnProperty(name = "controlei.ai.enabled", havingValue = "true")
public class OpenRouterAssistantAiClient implements AssistantAiClient {

    private final RestClient http;
    private final ObjectMapper mapper;
    private final String model;

    public OpenRouterAssistantAiClient(
            ObjectMapper mapper,
            @Value("${controlei.ai.api-key}") String apiKey,
            @Value("${controlei.ai.assistant-model:${controlei.ai.model:google/gemini-2.5-flash}}") String model,
            @Value("${controlei.ai.base-url:https://openrouter.ai/api/v1}") String baseUrl) {
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
    public Completion complete(List<Message> messages, List<ToolSpec> tools) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("temperature", 0.2);
        body.put("max_tokens", 700);
        body.put("provider", Map.of("data_collection", "deny"));
        body.put("messages", messages.stream().map(this::toWire).toList());
        if (!tools.isEmpty()) {
            body.put("tools", tools.stream().map(this::toWire).toList());
        }

        JsonNode response = http.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
        if (response == null) {
            throw new IllegalStateException("resposta vazia do provedor de IA");
        }
        JsonNode message = response.path("choices").path(0).path("message");
        List<ToolCall> calls = new ArrayList<>();
        for (JsonNode c : message.path("tool_calls")) {
            calls.add(new ToolCall(c.path("id").asString(""), c.path("function").path("name").asString(""),
                    c.path("function").path("arguments").asString("{}")));
        }
        return new Completion(message.path("content").asString(""), calls);
    }

    private Map<String, Object> toWire(Message m) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("role", m.role());
        out.put("content", m.content() == null ? "" : m.content());
        if (m.toolCallId() != null) {
            out.put("tool_call_id", m.toolCallId());
        }
        if (!m.toolCalls().isEmpty()) {
            out.put("tool_calls", m.toolCalls().stream().map(c -> Map.of(
                    "id", c.id(),
                    "type", "function",
                    "function", Map.of("name", c.name(), "arguments", c.argumentsJson()))).toList());
        }
        return out;
    }

    private Map<String, Object> toWire(ToolSpec t) {
        return Map.of("type", "function", "function", Map.of(
                "name", t.name(),
                "description", t.description(),
                "parameters", mapper.readTree(t.parametersJsonSchema())));
    }
}
