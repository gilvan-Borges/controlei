package br.com.controlei.application.services.assistant;

import br.com.controlei.application.exceptions.NotFoundException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Acoes preparadas pelo assistente e ainda nao confirmadas. Cada uma pertence a um usuario, vale por 10 minutos e so
 * pode ser usada uma vez: confirmar ou cancelar a remove. Em memoria e por instancia, como as demais cotas.
 */
@Component
public class PendingActions {

    static final Duration TTL = Duration.ofMinutes(10);
    static final int MAX_PER_USER = 5;

    private record Pending(UUID userId, String summary, Supplier<String> action, Instant expiresAt) {}

    public record Prepared(UUID id, String summary, boolean destructive) {}

    private final ConcurrentHashMap<UUID, Pending> store = new ConcurrentHashMap<>();
    private final Clock clock;

    public PendingActions() {
        this(Clock.systemUTC());
    }

    PendingActions(Clock clock) {
        this.clock = clock;
    }

    public Prepared register(UUID userId, String summary, Supplier<String> action, boolean destructive) {
        Instant now = clock.instant();
        store.values().removeIf(p -> p.expiresAt().isBefore(now));
        long mine = store.values().stream().filter(p -> p.userId().equals(userId)).count();
        if (mine >= MAX_PER_USER) {
            throw new AssistantTool.ToolException("Ha acoes demais aguardando confirmacao. Peca ao usuario para confirmar ou cancelar as pendentes.");
        }
        UUID id = UUID.randomUUID();
        store.put(id, new Pending(userId, summary, action, now.plus(TTL)));
        return new Prepared(id, summary, destructive);
    }

    /** Retira a acao (uso unico). So o dono a retira; qualquer outro recebe "nao encontrada", sem pista de que existe. */
    public Supplier<String> take(UUID id, UUID userId) {
        Pending p = store.get(id);
        if (p == null || !p.userId().equals(userId) || p.expiresAt().isBefore(clock.instant())
                || !store.remove(id, p)) {
            throw new NotFoundException("Acao nao encontrada ou expirada");
        }
        return p.action();
    }
}
