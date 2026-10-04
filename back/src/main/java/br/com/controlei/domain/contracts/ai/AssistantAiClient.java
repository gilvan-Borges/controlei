package br.com.controlei.domain.contracts.ai;

import java.util.List;

/**
 * Porta para o modelo de linguagem do assistente. O dominio so conhece "mensagens entram, texto e/ou pedidos de
 * ferramenta saem"; qual provedor atende fica na infraestrutura. O modelo nunca executa nada: ele so PEDE ferramentas,
 * e quem decide, valida e executa e o codigo da aplicacao.
 */
public interface AssistantAiClient {

    /** Ferramenta oferecida ao modelo; {@code parametersJsonSchema} e um JSON Schema de objeto, em texto. */
    record ToolSpec(String name, String description, String parametersJsonSchema) {}

    /** Pedido de execucao feito pelo modelo. {@code argumentsJson} e texto bruto e nao confiavel. */
    record ToolCall(String id, String name, String argumentsJson) {}

    /** Mensagem da conversa. {@code role}: system, user, assistant ou tool. */
    record Message(String role, String content, List<ToolCall> toolCalls, String toolCallId) {
        public static Message system(String text) {
            return new Message("system", text, List.of(), null);
        }

        public static Message user(String text) {
            return new Message("user", text, List.of(), null);
        }

        public static Message assistant(String text) {
            return new Message("assistant", text, List.of(), null);
        }

        public static Message assistantCalls(String text, List<ToolCall> calls) {
            return new Message("assistant", text, List.copyOf(calls), null);
        }

        public static Message tool(String callId, String result) {
            return new Message("tool", result, List.of(), callId);
        }
    }

    /** Resposta do modelo: texto final, pedidos de ferramenta, ou os dois. */
    record Completion(String content, List<ToolCall> toolCalls) {}

    Completion complete(List<Message> messages, List<ToolSpec> tools);
}
