package br.com.controlei.infrastructure.kafka.consumers;

import br.com.controlei.application.services.BudgetAlertService;
import br.com.controlei.infrastructure.redis.RedisIdempotencyService;
import br.com.controlei.shared.events.KafkaTopics;
import br.com.controlei.shared.events.TransactionCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada do Kafka: so cuida de mensageria. A regra do alerta esta em {@link BudgetAlertService}.
 *
 * <p>Idempotencia: a chave do evento e reservada no Redis antes de processar; se o processamento falhar, a reserva e
 * desfeita e a excecao sobe, para o tratador de erros do listener retentar (e, esgotadas as tentativas, mandar para o
 * topico .DLT). Sem esse "desfazer", uma falha transitoria descartaria o evento para sempre ao ser reentregue.
 */
@Component
public class BudgetMonitoringConsumer {

    private final RedisIdempotencyService idempotencyService;
    private final BudgetAlertService budgetAlertService;

    public BudgetMonitoringConsumer(RedisIdempotencyService idempotencyService,
                                    BudgetAlertService budgetAlertService) {
        this.idempotencyService = idempotencyService;
        this.budgetAlertService = budgetAlertService;
    }

    @KafkaListener(topics = KafkaTopics.TRANSACTIONS, groupId = "controlei-budget-monitoring-group")
    public void onTransactionCreated(TransactionCreatedEvent event) {
        if (event == null || !idempotencyService.acquireIdempotency(event.getEventId())) {
            return;
        }
        try {
            budgetAlertService.evaluate(event);
        } catch (RuntimeException e) {
            idempotencyService.release(event.getEventId());
            throw e;
        }
    }
}
