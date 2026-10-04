package br.com.controlei.infrastructure.outbox;

import br.com.controlei.shared.events.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Entrega ao Kafka os eventos gravados no outbox (padrao Transactional Outbox, lado "relay").
 *
 * <p>Garantias e limites, ditos com clareza:
 * <ul>
 *   <li><b>Pelo menos uma vez.</b> Se o envio der certo e o processo cair antes de marcar a linha como publicada, o
 *       evento sai de novo. Por isso o consumidor e idempotente (chave do evento no Redis).</li>
 *   <li><b>Ordem.</b> As linhas saem na ordem da coluna {@code seq}, e a primeira falha interrompe o lote: um
 *       evento posterior nunca ultrapassa um anterior que ainda nao foi entregue. Limite: {@code seq} e a ordem de
 *       gravacao, que coincide com a de commit, exceto quando duas transacoes concorrentes da mesma familia
 *       confirmam fora de ordem; garantir isso exigiria travar a familia, e o custo nao compensa aqui.</li>
 *   <li><b>Varias instancias.</b> {@code FOR UPDATE SKIP LOCKED} faz cada relay pegar linhas diferentes, sem
 *       duplicar trabalho.</li>
 *   <li><b>Mensagem venenosa.</b> Depois de {@value #MAX_ATTEMPTS} falhas a linha vira "morta" e sai da fila, para
 *       nao travar todos os eventos seguintes. Ela continua na tabela para inspecao.</li>
 * </ul>
 */
@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    static final int BATCH_SIZE = 100;
    static final int MAX_ATTEMPTS = 10;
    private static final long SEND_TIMEOUT_SECONDS = 5;
    /** So classes deste pacote sao reconstruidas: o nome da classe vem do banco e nao pode instanciar qualquer coisa. */
    private static final String ALLOWED_PACKAGE = "br.com.controlei.shared.events.";

    private record Row(UUID id, String topic, String key, String eventClass, String payload, int attempts) {}

    private final JdbcTemplate jdbc;
    private final KafkaTemplate<String, Object> kafka;
    private final ObjectMapper mapper;
    private final int retentionDays;

    public OutboxRelay(JdbcTemplate jdbc,
                       KafkaTemplate<String, Object> kafka,
                       ObjectMapper mapper,
                       @Value("${app.outbox.retention-days:7}") int retentionDays) {
        this.jdbc = jdbc;
        this.kafka = kafka;
        this.mapper = mapper;
        this.retentionDays = retentionDays;
    }

    @Scheduled(fixedDelayString = "${app.outbox.relay.interval-ms:1000}")
    public void scheduledDrain() {
        try {
            drain();
        } catch (RuntimeException e) {
            log.error("Falha no relay do outbox: {}", e.getClass().getSimpleName());
        }
    }

    /** @return quantos eventos foram entregues neste lote */
    @Transactional
    public int drain() {
        List<Row> rows = jdbc.query("""
                        SELECT id, topic, partition_key, event_class, payload, attempts
                        FROM outbox_events
                        WHERE published_at IS NULL AND dead_at IS NULL
                        ORDER BY seq
                        LIMIT ?
                        FOR UPDATE SKIP LOCKED""",
                (rs, n) -> new Row(rs.getObject("id", UUID.class), rs.getString("topic"), rs.getString("partition_key"),
                        rs.getString("event_class"), rs.getString("payload"), rs.getInt("attempts")),
                BATCH_SIZE);

        int sent = 0;
        for (Row row : rows) {
            try {
                DomainEvent event = deserialize(row);
                kafka.send(row.topic(), row.key(), event).get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                jdbc.update("UPDATE outbox_events SET published_at = ? WHERE id = ?", now(), row.id());
                sent++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                int attempts = row.attempts() + 1;
                boolean dead = attempts >= MAX_ATTEMPTS || e instanceof IllegalArgumentException;
                jdbc.update("UPDATE outbox_events SET attempts = ?, dead_at = ? WHERE id = ?",
                        attempts, dead ? now() : null, row.id());
                log.error("Evento {} nao entregue (tentativa {}{}): {}", row.id(), attempts,
                        dead ? ", descartado da fila" : "", e.getClass().getSimpleName());
                break; // preserva a ordem: nenhum evento posterior passa na frente deste
            }
        }
        return sent;
    }

    /** Remove da tabela o que ja foi entregue ha mais que a retencao; roda de madrugada. */
    @Scheduled(cron = "${app.outbox.purge-cron:0 30 3 * * *}")
    @Transactional
    public int purge() {
        return jdbc.update("DELETE FROM outbox_events WHERE published_at < ?",
                Timestamp.valueOf(LocalDateTime.now().minusDays(retentionDays)));
    }

    private DomainEvent deserialize(Row row) {
        if (!row.eventClass().startsWith(ALLOWED_PACKAGE)) {
            throw new IllegalArgumentException("classe de evento fora do pacote permitido");
        }
        try {
            Class<?> type = Class.forName(row.eventClass());
            if (!DomainEvent.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException("classe nao e um DomainEvent");
            }
            return (DomainEvent) mapper.readValue(row.payload(), type);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("classe de evento desconhecida", e);
        }
    }

    private static Timestamp now() {
        return Timestamp.valueOf(LocalDateTime.now());
    }
}
