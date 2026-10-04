package br.com.controlei.application.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Confere a assinatura HMAC-SHA256 do corpo de um webhook. Sem segredo configurado, NENHUM webhook e aceito:
 * um endpoint publico sem verificacao deixaria qualquer pessoa disparar efeitos no sistema.
 */
@Component
public class WebhookSignatureVerifier {

    private final String secret;

    public WebhookSignatureVerifier(@Value("${app.openfinance.webhook-secret:}") String secret) {
        this.secret = secret;
    }

    public boolean configured() {
        return !secret.isBlank();
    }

    /** @param signature valor do cabecalho, no formato "sha256=<hex>" (o prefixo e opcional) */
    public boolean isValid(String body, String signature) {
        if (!configured() || body == null || signature == null) {
            return false;
        }
        String provided = signature.startsWith("sha256=") ? signature.substring(7) : signature;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String expected = HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
            // Comparacao em tempo constante: nao vaza, byte a byte, quanto da assinatura estava certo
            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8), provided.toLowerCase().getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            return false;
        }
    }
}
