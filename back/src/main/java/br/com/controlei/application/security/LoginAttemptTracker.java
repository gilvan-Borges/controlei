package br.com.controlei.application.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Trava tentativas de login por e-mail depois de falhas seguidas. O limite por IP do gateway nao basta: um ataque
 * distribuido contra UMA conta usa muitos IPs. A chave e o e-mail informado (existindo ou nao), entao o bloqueio
 * tambem nao revela se a conta existe.
 *
 * <p>Em memoria e por instancia, com tamanho maximo: suficiente para uma instancia unica; com varias, o contador
 * passaria para o Redis.
 */
@Component
public class LoginAttemptTracker {

    private record Attempts(int failures, Instant lockedUntil, Instant lastFailure) {}

    private static final int MAX_TRACKED = 10_000;

    private final ConcurrentHashMap<String, Attempts> attempts = new ConcurrentHashMap<>();
    private final int maxFailures;
    private final Duration lockFor;
    private final Clock clock;

    @Autowired
    public LoginAttemptTracker(@Value("${app.security.login.max-failures:5}") int maxFailures,
                               @Value("${app.security.login.lock-minutes:15}") long lockMinutes) {
        this(maxFailures, Duration.ofMinutes(lockMinutes), Clock.systemUTC());
    }

    LoginAttemptTracker(int maxFailures, Duration lockFor, Clock clock) {
        this.maxFailures = maxFailures;
        this.lockFor = lockFor;
        this.clock = clock;
    }

    public boolean isLocked(String key) {
        Attempts a = attempts.get(key);
        return a != null && a.lockedUntil() != null && clock.instant().isBefore(a.lockedUntil());
    }

    public void recordFailure(String key) {
        Instant now = clock.instant();
        attempts.compute(key, (k, current) -> {
            // Falhas antigas (fora da janela de bloqueio) nao se acumulam para sempre
            int failures = current == null || current.lastFailure().plus(lockFor).isBefore(now) ? 1 : current.failures() + 1;
            Instant lockedUntil = failures >= maxFailures ? now.plus(lockFor) : null;
            return new Attempts(failures, lockedUntil, now);
        });
        if (attempts.size() > MAX_TRACKED) {
            attempts.entrySet().removeIf(e -> e.getValue().lastFailure().plus(lockFor).isBefore(now));
        }
    }

    public void recordSuccess(String key) {
        attempts.remove(key);
    }
}
