package br.com.controlei.infrastructure.outbox;

import br.com.controlei.application.contracts.DomainEventPublisher;
import br.com.controlei.shared.events.DomainEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Grava o evento na tabela outbox_events, na transacao de quem chamou. Nada vai ao Kafka aqui: o {@link OutboxRelay}
 * entrega depois do commit.
 */
@Component
public class OutboxEventPublisher implements DomainEventPublisher {

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public OutboxEventPublisher(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    /** MANDATORY: sem transacao ativa lanca excecao, em vez de gravar o evento "solto" e perder a atomicidade. */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(String topic, DomainEvent event) {
        String partitionKey = event.getFamilyId() != null
                ? event.getFamilyId().toString()
                : event.getEventId().toString();
        jdbc.update("""
                        INSERT INTO outbox_events
                            (id, topic, partition_key, event_type, event_class, payload, attempts, created_at)
                        VALUES (?, ?, ?, ?, ?, ?, 0, ?)""",
                event.getEventId() != null ? event.getEventId() : UUID.randomUUID(),
                topic,
                partitionKey,
                event.getEventType(),
                event.getClass().getName(),
                mapper.writeValueAsString(event),
                Timestamp.valueOf(LocalDateTime.now()));
    }
}
