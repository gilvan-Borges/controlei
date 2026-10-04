package br.com.controlei.infrastructure.configurations.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Impede subir com um segredo de JWT fraco. Em producao, alem do tamanho minimo, recusa os valores de exemplo que
 * circulam no repositorio ("troque-por...", "dev-only...") e segredos com pouca variedade de caracteres: copiar o
 * .env.example sem trocar a chave deixaria qualquer pessoa forjar tokens.
 */
@Component
public class JwtSecretValidator {

    static final int MIN_LENGTH = 32;
    static final int MIN_DISTINCT_CHARS = 12;
    private static final List<String> PLACEHOLDERS = List.of("troque-por", "dev-only", "change-this", "changeme", "example");

    private final String jwtSecret;
    private final boolean production;

    public JwtSecretValidator(@Value("${jwt.secret}") String jwtSecret, Environment environment) {
        this.jwtSecret = jwtSecret;
        this.production = environment.acceptsProfiles(Profiles.of("prod"));
    }

    @PostConstruct
    public void validate() {
        check(jwtSecret, production);
    }

    static void check(String secret, boolean production) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("A variavel de ambiente JWT_SECRET e obrigatoria e nao pode estar vazia");
        }
        if (secret.length() < MIN_LENGTH) {
            throw new IllegalStateException(
                    "JWT_SECRET deve ter no minimo " + MIN_LENGTH + " caracteres para garantir seguranca adequada");
        }
        if (!production) {
            return;
        }
        String lower = secret.toLowerCase(Locale.ROOT);
        if (PLACEHOLDERS.stream().anyMatch(lower::contains)) {
            throw new IllegalStateException("JWT_SECRET ainda e um valor de exemplo; gere um segredo aleatorio (openssl rand -base64 48)");
        }
        if (secret.chars().distinct().count() < MIN_DISTINCT_CHARS) {
            throw new IllegalStateException("JWT_SECRET tem pouca variedade de caracteres; gere um segredo aleatorio");
        }
    }
}
