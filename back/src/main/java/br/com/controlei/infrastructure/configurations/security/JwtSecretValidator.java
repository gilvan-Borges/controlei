package br.com.controlei.infrastructure.configurations.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtSecretValidator {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @PostConstruct
    public void validate() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException(
                    "A variavel de ambiente JWT_SECRET e obrigatoria e nao pode estar vazia"
            );
        }

        if (jwtSecret.length() < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET deve ter no minimo 32 caracteres para garantir seguranca adequada"
            );
        }
    }
}
