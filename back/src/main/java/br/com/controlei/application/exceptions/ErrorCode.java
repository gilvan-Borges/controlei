package br.com.controlei.application.exceptions;

public enum ErrorCode {

    VALIDATION_ERROR("VALIDATION_ERROR", "Dados invalidos"),
    BUSINESS_ERROR("BUSINESS_ERROR", "Erro de negocio"),
    NOT_FOUND("NOT_FOUND", "Recurso nao encontrado"),
    FORBIDDEN("FORBIDDEN", "Acesso negado"),
    UNAUTHORIZED("UNAUTHORIZED", "Nao autorizado"),
    INTERNAL_ERROR("INTERNAL_ERROR", "Erro interno do servidor");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
