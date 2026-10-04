package br.com.controlei.application.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Refresh token: o valor que o cliente guarda nunca fica no banco, so o SHA-256 dele. Quem ler a tabela (backup,
 * injecao, acesso indevido) nao consegue usar os tokens. Como o token tem 256 bits aleatorios, um hash simples basta;
 * o custo de um hash lento (BCrypt) so faria sentido para segredos de baixa entropia, como senhas.
 */
public final class TokenHasher {

    private static final SecureRandom RANDOM = new SecureRandom();

    private TokenHasher() {
    }

    /** 32 bytes aleatorios em Base64 URL-safe, sem padding. */
    public static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String sha256(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }
}
