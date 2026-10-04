package br.com.controlei.application.services.assistant;

import br.com.controlei.application.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PendingActionsTest {

    private final UUID owner = UUID.randomUUID();

    @Test
    void anActionCanBeUsedOnlyOnce() {
        var pending = new PendingActions();
        var prepared = pending.register(owner, "resumo", () -> "ok", false);

        assertEquals("ok", pending.take(prepared.id(), owner).get());
        assertThrows(NotFoundException.class, () -> pending.take(prepared.id(), owner));
    }

    @Test
    void anotherUserCannotTakeItAndGetsNoHintItExists() {
        var pending = new PendingActions();
        var prepared = pending.register(owner, "resumo", () -> "ok", false);

        assertThrows(NotFoundException.class, () -> pending.take(prepared.id(), UUID.randomUUID()));
        // E o dono ainda consegue: a tentativa alheia nao a consome.
        assertEquals("ok", pending.take(prepared.id(), owner).get());
    }

    @Test
    void expiresAfterTheTtl() {
        var now = new Instant[]{Instant.parse("2026-10-04T12:00:00Z")};
        Clock clock = new Clock() {
            @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
            @Override public Clock withZone(java.time.ZoneId zone) { return this; }
            @Override public Instant instant() { return now[0]; }
        };
        var pending = new PendingActions(clock);
        var prepared = pending.register(owner, "resumo", () -> "ok", false);

        now[0] = now[0].plus(PendingActions.TTL).plus(Duration.ofSeconds(1));

        assertThrows(NotFoundException.class, () -> pending.take(prepared.id(), owner));
    }

    @Test
    void limitsHowManyActionsOneUserCanHaveWaiting() {
        var pending = new PendingActions();
        for (int i = 0; i < PendingActions.MAX_PER_USER; i++) {
            pending.register(owner, "a" + i, () -> "ok", false);
        }

        assertThrows(AssistantTool.ToolException.class, () -> pending.register(owner, "demais", () -> "ok", false));
        // Outro usuario nao e afetado.
        pending.register(UUID.randomUUID(), "outro", () -> "ok", false);
    }
}
