package br.com.controlei.application.exceptions;

/**
 * Erro da rota de voz, com o status HTTP que ele deve ter (400, 413, 422, 429 ou 503). Vira ProblemDetail no
 * GlobalExceptionHandler. A mensagem e para a pessoa: nunca carrega trecho do audio nem da transcricao.
 */
public class VoiceException extends RuntimeException {

    private final int status;
    private final String title;

    public VoiceException(int status, String title, String message) {
        super(message);
        this.status = status;
        this.title = title;
    }

    public int getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public static VoiceException badRequest(String message) {
        return new VoiceException(400, "Audio invalido", message);
    }

    public static VoiceException tooLarge(String message) {
        return new VoiceException(413, "Audio grande demais", message);
    }

    public static VoiceException notUnderstood(String message) {
        return new VoiceException(422, "Audio nao compreendido", message);
    }

    public static VoiceException quotaExceeded(String message) {
        return new VoiceException(429, "Cota de voz esgotada", message);
    }

    public static VoiceException unavailable(String message) {
        return new VoiceException(503, "Voz indisponivel", message);
    }
}
