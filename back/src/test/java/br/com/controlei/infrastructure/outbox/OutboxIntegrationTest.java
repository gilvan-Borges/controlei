package br.com.controlei.infrastructure.outbox;

import br.com.controlei.application.contracts.DomainEventPublisher;
import br.com.controlei.shared.events.KafkaTopics;
import br.com.controlei.shared.events.TransactionCreatedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Transactional Outbox de ponta a ponta contra o banco: atomicidade com o dado de negocio, ordem de entrega,
 * retentativa e mensagem venenosa. O Kafka e substituido por um dublê; o que se testa aqui e a garantia do padrao.
 *
 * <p>Esta classe NAO e @Transactional: o commit e o rollback precisam acontecer de verdade.
 */
@SpringBootTest
@ActiveProfiles("test")
class OutboxIntegrationTest {

    @Autowired
    private DomainEventPublisher publisher;

    @Autowired
    private OutboxRelay relay;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TransactionTemplate tx;

    @MockitoBean
    private KafkaTemplate<String, Object> kafka;

    private final List<Object> delivered = new ArrayList<>();

    @BeforeEach
    @AfterEach
    void clean() {
        jdbc.update("DELETE FROM outbox_events");
        delivered.clear();
    }

    private void kafkaAccepts() {
        when(kafka.send(anyString(), anyString(), any())).thenAnswer(inv -> {
            delivered.add(inv.getArgument(2));
            return CompletableFuture.completedFuture(null);
        });
    }

    private static TransactionCreatedEvent event(UUID family, String description) {
        return TransactionCreatedEvent.builder()
                .transactionId(UUID.randomUUID())
                .familyId(family)
                .userId(UUID.randomUUID())
                .amount(new BigDecimal("12.34"))
                .type("EXPENSE")
                .description(description)
                .transactionDate(LocalDate.of(2026, 10, 4))
                .build();
    }

    private int rows(String where) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM outbox_events WHERE " + where, Integer.class);
    }

    @Test
    void publishingOutsideATransactionFailsInsteadOfWritingALooseEvent() {
        assertThatThrownBy(() -> publisher.publish(KafkaTopics.TRANSACTIONS, event(UUID.randomUUID(), "x")))
                .isInstanceOf(IllegalTransactionStateException.class);
        assertThat(rows("1 = 1")).isZero();
    }

    @Test
    void theEventIsCommittedTogetherWithTheTransaction() {
        UUID family = UUID.randomUUID();
        tx.executeWithoutResult(s -> publisher.publish(KafkaTopics.TRANSACTIONS, event(family, "gravado")));

        assertThat(rows("published_at IS NULL")).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT partition_key FROM outbox_events", String.class))
                .isEqualTo(family.toString());
    }

    @Test
    void aRolledBackTransactionLeavesNoEventBehind() {
        tx.executeWithoutResult(s -> {
            publisher.publish(KafkaTopics.TRANSACTIONS, event(UUID.randomUUID(), "fantasma"));
            s.setRollbackOnly(); // o dado de negocio sofreu rollback: o evento nao pode existir
        });

        assertThat(rows("1 = 1")).isZero();
        assertThat(relay.drain()).isZero();
        verify(kafka, never()).send(anyString(), anyString(), any());
    }

    @Test
    void drainDeliversInOrderOnceAndMarksThemPublished() {
        kafkaAccepts();
        UUID family = UUID.randomUUID();
        tx.executeWithoutResult(s -> {
            publisher.publish(KafkaTopics.TRANSACTIONS, event(family, "primeiro"));
            publisher.publish(KafkaTopics.TRANSACTIONS, event(family, "segundo"));
            publisher.publish(KafkaTopics.TRANSACTIONS, event(family, "terceiro"));
        });

        assertThat(relay.drain()).isEqualTo(3);
        assertThat(delivered).extracting(e -> ((TransactionCreatedEvent) e).getDescription())
                .containsExactly("primeiro", "segundo", "terceiro");
        assertThat(rows("published_at IS NOT NULL")).isEqualTo(3);

        assertThat(relay.drain()).isZero(); // nada e entregue duas vezes
        verify(kafka, times(3)).send(eq(KafkaTopics.TRANSACTIONS), eq(family.toString()), any(TransactionCreatedEvent.class));
    }

    @Test
    void theReconstructedEventIsEqualToTheOneThatWasPublished() {
        kafkaAccepts();
        TransactionCreatedEvent original = event(UUID.randomUUID(), "ida e volta");
        tx.executeWithoutResult(s -> publisher.publish(KafkaTopics.TRANSACTIONS, original));

        relay.drain();

        assertThat(delivered).singleElement().isEqualTo(original);
    }

    @Test
    void aFailureStopsTheBatchSoNoLaterEventOvertakesAnEarlierOne() {
        UUID family = UUID.randomUUID();
        tx.executeWithoutResult(s -> {
            publisher.publish(KafkaTopics.TRANSACTIONS, event(family, "falha"));
            publisher.publish(KafkaTopics.TRANSACTIONS, event(family, "depois"));
        });
        when(kafka.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("broker fora")));

        assertThat(relay.drain()).isZero();

        assertThat(rows("published_at IS NOT NULL")).isZero();
        assertThat(jdbc.queryForObject("SELECT MAX(attempts) FROM outbox_events", Integer.class)).isEqualTo(1);
        verify(kafka).send(anyString(), anyString(), any()); // so tentou o primeiro; o segundo esperou

        // o broker volta: sai tudo, na ordem
        kafkaAccepts();
        assertThat(relay.drain()).isEqualTo(2);
        assertThat(delivered).extracting(e -> ((TransactionCreatedEvent) e).getDescription())
                .containsExactly("falha", "depois");
    }

    @Test
    void aPoisonMessageIsParkedAfterTheMaximumAttemptsSoItDoesNotBlockTheQueue() {
        UUID family = UUID.randomUUID();
        tx.executeWithoutResult(s -> {
            publisher.publish(KafkaTopics.TRANSACTIONS, event(family, "venenosa"));
            publisher.publish(KafkaTopics.TRANSACTIONS, event(family, "saudavel"));
        });
        when(kafka.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("sempre falha")));

        for (int i = 0; i < OutboxRelay.MAX_ATTEMPTS; i++) {
            relay.drain();
        }
        assertThat(rows("dead_at IS NOT NULL")).isEqualTo(1);

        kafkaAccepts();
        assertThat(relay.drain()).isEqualTo(1);
        assertThat(delivered).extracting(e -> ((TransactionCreatedEvent) e).getDescription())
                .containsExactly("saudavel");
        assertThat(rows("dead_at IS NOT NULL")).isEqualTo(1); // a venenosa fica na tabela para inspecao
    }

    @Test
    void anEventClassOutsideTheAllowedPackageIsNeverInstantiated() {
        jdbc.update("""
                INSERT INTO outbox_events (id, topic, partition_key, event_type, event_class, payload, attempts, created_at)
                VALUES (?, 't', 'k', 'X', 'java.lang.ProcessBuilder', '{}', 0, CURRENT_TIMESTAMP)""", UUID.randomUUID());
        kafkaAccepts();

        assertThat(relay.drain()).isZero();

        assertThat(rows("dead_at IS NOT NULL")).isEqualTo(1);
        assertThat(delivered).isEmpty();
    }

    @Test
    void purgeRemovesOnlyOldPublishedRows() {
        jdbc.update("""
                INSERT INTO outbox_events (id, topic, partition_key, event_type, event_class, payload, attempts, created_at, published_at)
                VALUES (?, 't', 'k', 'X', 'a.B', '{}', 0, TIMESTAMPADD(DAY, -30, CURRENT_TIMESTAMP), TIMESTAMPADD(DAY, -30, CURRENT_TIMESTAMP))""",
                UUID.randomUUID());
        jdbc.update("""
                INSERT INTO outbox_events (id, topic, partition_key, event_type, event_class, payload, attempts, created_at, published_at)
                VALUES (?, 't', 'k', 'X', 'a.B', '{}', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)""", UUID.randomUUID());
        jdbc.update("""
                INSERT INTO outbox_events (id, topic, partition_key, event_type, event_class, payload, attempts, created_at)
                VALUES (?, 't', 'k', 'X', 'a.B', '{}', 0, TIMESTAMPADD(DAY, -30, CURRENT_TIMESTAMP))""", UUID.randomUUID());

        assertThat(relay.purge()).isEqualTo(1);
        assertThat(rows("1 = 1")).isEqualTo(2); // a recente publicada e a antiga ainda nao publicada ficam
    }
}
