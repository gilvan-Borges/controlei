package br.com.controlei.application.security;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityPrimitivesTest {

    // ---- TokenHasher

    @Test
    void tokensAreLongRandomAndUnique() {
        String a = TokenHasher.newToken();
        String b = TokenHasher.newToken();

        assertThat(a).isNotEqualTo(b).hasSizeGreaterThanOrEqualTo(43).matches("[A-Za-z0-9_-]+");
    }

    @Test
    void theStoredValueIsAHashNotTheToken() {
        String token = TokenHasher.newToken();
        String stored = TokenHasher.sha256(token);

        assertThat(stored).isNotEqualTo(token).hasSize(64).matches("[0-9a-f]+");
        assertThat(TokenHasher.sha256(token)).isEqualTo(stored); // deterministico: e assim que se busca
        assertThat(TokenHasher.sha256("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"); // vetor conhecido
    }

    // ---- WebhookSignatureVerifier

    private static String sign(String secret, String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void acceptsATrueSignatureWithOrWithoutThePrefix() throws Exception {
        var verifier = new WebhookSignatureVerifier("segredo");
        String body = "{\"itemId\":\"x\"}";
        String signature = sign("segredo", body);

        assertThat(verifier.isValid(body, "sha256=" + signature)).isTrue();
        assertThat(verifier.isValid(body, signature)).isTrue();
    }

    @Test
    void rejectsATamperedBodyAWrongSecretAndMissingSignature() throws Exception {
        var verifier = new WebhookSignatureVerifier("segredo");
        String body = "{\"itemId\":\"x\"}";

        assertThat(verifier.isValid(body + " ", "sha256=" + sign("segredo", body))).isFalse();
        assertThat(verifier.isValid(body, "sha256=" + sign("outro", body))).isFalse();
        assertThat(verifier.isValid(body, null)).isFalse();
        assertThat(verifier.isValid(null, "sha256=00")).isFalse();
    }

    @Test
    void withoutASecretNoWebhookIsAccepted() throws Exception {
        var verifier = new WebhookSignatureVerifier("");

        assertThat(verifier.configured()).isFalse();
        assertThat(verifier.isValid("{}", "sha256=" + sign("qualquer", "{}"))).isFalse();
    }

    // ---- LoginAttemptTracker

    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-10-04T12:00:00Z");

        void advance(Duration d) {
            now = now.plus(d);
        }

        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    @Test
    void locksAfterTheConfiguredNumberOfFailuresAndUnlocksAfterTheWindow() {
        var clock = new MutableClock();
        var tracker = new LoginAttemptTracker(3, Duration.ofMinutes(15), clock);

        tracker.recordFailure("a@x.com");
        tracker.recordFailure("a@x.com");
        assertThat(tracker.isLocked("a@x.com")).isFalse();
        tracker.recordFailure("a@x.com");
        assertThat(tracker.isLocked("a@x.com")).isTrue();

        clock.advance(Duration.ofMinutes(16));
        assertThat(tracker.isLocked("a@x.com")).isFalse();
    }

    @Test
    void aSuccessClearsTheCounterAndEmailsAreIndependent() {
        var tracker = new LoginAttemptTracker(2, Duration.ofMinutes(15), new MutableClock());

        tracker.recordFailure("a@x.com");
        tracker.recordSuccess("a@x.com");
        tracker.recordFailure("a@x.com");
        assertThat(tracker.isLocked("a@x.com")).isFalse();

        tracker.recordFailure("b@x.com");
        tracker.recordFailure("b@x.com");
        assertThat(tracker.isLocked("b@x.com")).isTrue();
        assertThat(tracker.isLocked("a@x.com")).isFalse();
    }
}
