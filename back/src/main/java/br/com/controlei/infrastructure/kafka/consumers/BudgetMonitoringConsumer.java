package br.com.controlei.infrastructure.kafka.consumers;

import br.com.controlei.domain.contracts.repositories.BudgetRepositoryPort;
import br.com.controlei.domain.contracts.repositories.NotificationRepositoryPort;
import br.com.controlei.domain.contracts.repositories.TransactionRepositoryPort;
import br.com.controlei.domain.models.entities.Budget;
import br.com.controlei.domain.models.entities.Notification;
import br.com.controlei.domain.models.entities.Transaction;
import br.com.controlei.domain.models.enums.NotificationType;
import br.com.controlei.domain.models.enums.TransactionType;
import br.com.controlei.infrastructure.kafka.EventPublisher;
import br.com.controlei.infrastructure.redis.RedisIdempotencyService;
import br.com.controlei.shared.events.BudgetExceededEvent;
import br.com.controlei.shared.events.KafkaTopics;
import br.com.controlei.shared.events.TransactionCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class BudgetMonitoringConsumer {

    private static final Logger log = LoggerFactory.getLogger(BudgetMonitoringConsumer.class);

    private final BudgetRepositoryPort budgetRepository;
    private final TransactionRepositoryPort transactionRepository;
    private final NotificationRepositoryPort notificationRepository;
    private final RedisIdempotencyService idempotencyService;
    private final EventPublisher eventPublisher;

    public BudgetMonitoringConsumer(BudgetRepositoryPort budgetRepository,
                                  TransactionRepositoryPort transactionRepository,
                                  NotificationRepositoryPort notificationRepository,
                                  RedisIdempotencyService idempotencyService,
                                  EventPublisher eventPublisher) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
        this.notificationRepository = notificationRepository;
        this.idempotencyService = idempotencyService;
        this.eventPublisher = eventPublisher;
    }

    @KafkaListener(
            topics = KafkaTopics.TRANSACTIONS,
            groupId = "controlei-budget-monitoring-group"
    )
    public void onTransactionCreated(TransactionCreatedEvent event) {
        if (event == null || !idempotencyService.acquireIdempotency(event.getEventId())) {
            return;
        }

        if (!"EXPENSE".equalsIgnoreCase(event.getType()) || event.getCategoryId() == null) {
            return;
        }

        LocalDate date = event.getTransactionDate() != null ? event.getTransactionDate() : LocalDate.now();
        int month = date.getMonthValue();
        int year = date.getYear();

        Optional<Budget> budgetOpt = budgetRepository.findByFamilyIdAndUserIdAndCategoryIdAndYearAndMonthAndDeletedAtIsNull(
                event.getFamilyId(), event.getUserId(), event.getCategoryId(), year, month
        );

        if (budgetOpt.isEmpty()) {
            return;
        }

        Budget budget = budgetOpt.get();
        BigDecimal plannedAmount = budget.getPlannedAmount();
        if (plannedAmount == null || plannedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        LocalDate startMonth = LocalDate.of(year, month, 1);
        LocalDate endMonth = startMonth.plusMonths(1).minusDays(1);

        List<Transaction> transactions = transactionRepository.findAllByFamilyIdAndPeriod(event.getFamilyId(), startMonth, endMonth);
        BigDecimal totalSpent = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE && event.getCategoryId().equals(t.getCategoryId()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double percentage = totalSpent.divide(plannedAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();

        double threshold = budget.getAlertThresholdPercent() > 0 
                ? (double) budget.getAlertThresholdPercent() 
                : 80.0;

        if (percentage >= threshold) {
            log.warn("Alerta de Orçamento: Família [{}] atingiu {}% do teto na categoria [{}]", 
                    event.getFamilyId(), String.format("%.1f", percentage), event.getCategoryName());

            // 1. Emite evento de orçamento estourado no Kafka
            BudgetExceededEvent alertEvent = BudgetExceededEvent.builder()
                    .budgetId(budget.getId())
                    .familyId(event.getFamilyId())
                    .categoryId(event.getCategoryId())
                    .categoryName(event.getCategoryName())
                    .plannedAmount(plannedAmount)
                    .currentSpent(totalSpent)
                    .currentPercentage(percentage)
                    .thresholdPercentage(threshold)
                    .build();

            eventPublisher.publish(KafkaTopics.BUDGETS, alertEvent);

            // 2. Persiste notificação para o usuário
            Notification notification = new Notification(
                    UUID.randomUUID(),
                    event.getFamilyId(),
                    event.getUserId(),
                    "Alerta de Orçamento: " + (event.getCategoryName() != null ? event.getCategoryName() : "Categoria"),
                    String.format("Você atingiu %.1f%% do teto planejado (R$ %s de R$ %s).",
                            percentage, totalSpent.toPlainString(), plannedAmount.toPlainString()),
                    NotificationType.BUDGET_WARNING,
                    "/budgets",
                    false,
                    null,
                    LocalDateTime.now(),
                    null,
                    null,
                    null
            );

            notificationRepository.save(notification);
        }
    }
}
