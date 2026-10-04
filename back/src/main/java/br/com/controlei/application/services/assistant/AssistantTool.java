package br.com.controlei.application.services.assistant;

import tools.jackson.databind.JsonNode;

import java.util.function.Supplier;

/**
 * Uma ferramenta que o modelo pode pedir. Leitura devolve dados na hora; escrita nunca executa: devolve um resumo
 * montado pelo SERVIDOR (nao pelo modelo) e uma acao que so roda depois que a pessoa confirma na tela.
 */
public record AssistantTool(String name, String description, String schema, boolean writes, Handler handler) {

    @FunctionalInterface
    public interface Handler {
        Result handle(JsonNode args);
    }

    /** {@code data} para leitura; {@code summary} + {@code action} para escrita. */
    public record Result(String data, String summary, Supplier<String> action) {
        public static Result data(String data) {
            return new Result(data, null, null);
        }

        public static Result pending(String summary, Supplier<String> action) {
            return new Result(null, summary, action);
        }

        public boolean isPending() {
            return action != null;
        }
    }

    /** Erro que o modelo pode ler e corrigir (parametro faltando, nome ambiguo). */
    public static class ToolException extends RuntimeException {
        public ToolException(String message) {
            super(message);
        }
    }
}
