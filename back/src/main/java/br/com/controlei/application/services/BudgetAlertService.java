package br.com.controlei.application.services;

import br.com.controlei.application.contracts.DomainEventPublisher;
import br.com.controlei.domain.contracts.repositories.BudgetRepositoryPort;
import br.com.controlei.domain.contracts.repositories.NotificationRepositoryPort;
import br.com.controlei.domain.contracts.repositories.TransactionRepositoryPort;
import br.com.controlei.domain.models.entities.Budget;
import br.com.controlei.domain.models.entities.Notification;
import br.com.controlei.domain.models.enums.NotificationType;
import br.com.controlei.shared.events.BudgetExceededEvent;
import br.com.controlei.shared.events.KafkaTopics;
import br.com.controlei.shared.events.TransactionCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Regra do alerta de orcamento: depois de uma despesa, confere se a categoria atingiu o limite do mes e, se sim,
 * grava a notificacao e emite o evento no outbox.
 *
 * <p>Esta regra morava no consumidor do Kafka (camada de infraestrutura). Aqui ela fica na camada de aplicacao, testavel
 * sem broker; o consumidor so cuida de mensageria (idempotencia, retentativa). O total gasto vem de uma soma no banco,
 * nao de uma lista de transacoes do mes carregada em memoria a cada evento.
 */
@Service
public class BudgetAlertService {

    private static final Logger log = LoggerFactory.getLogger(BudgetAlertService.class);
    private static final BigDecimal DEFAULT_THRESHOLD = BigDecimal.valueOf(80);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final BudgetRepositoryPort budgetRepository;
    private final TransactionRepositoryPort transactionRepository;
    private final NotificationRepositoryPort notificationRepository;
    private final DomainEventPublisher eventPublisher;

    public BudgetAlertService(BudgetRepositoryPort budgetRepository,
                              TransactionRepositoryPort transactionRepository,
                              NotificationRepositoryPort notificationRepository,
                              DomainEventPublisher eventPublisher) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
        this.notificationRepository = notificationRepository;
        this.eventPublisher = eventPublisher;
    }

    /** @return true se um alerta foi emitido */
    @Transactional
    public boolean evaluate(TransactionCreatedEvent event) {
        if (!"EXPENSE".equalsIgnoreCase(event.getType()) || event.getCategoryId() == null) {
            return false;
        }

        LocalDate date = event.getTransactionDate() != null ? event.getTransactionDate() : LocalDate.now();
        Optional<Budget> found = budgetRepository.findByFamilyIdAndUserIdAndCategoryIdAndYearAndMonthAndDeletedAtIsNull(
                event.getFamilyId(), event.getUserId(), event.getCategoryId(), date.getYear(), date.getMonthValue());
        if (found.isEmpty()) {
            return false;
        }

        Budget budget = found.get();
        BigDecimal planned = budget.getPlannedAmount();
        if (planned == null || planned.signum() <= 0) {
            return false;
        }

        LocalDate start = date.withDayOfMonth(1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        BigDecimal spent = transactionRepository.sumExpenses(
                event.getFamilyId(), event.getCategoryId(), budget.getUserId(), start, end);

        BigDecimal percentage = spent.multiply(HUNDRED).divide(planned, 2, RoundingMode.HALF_UP);
        BigDecimal threshold = budget.getAlertThresholdPercent() > 0
                ? BigDecimal.valueOf(budget.getAlertThresholdPercent())
                : DEFAULT_THRESHOLD;
        if (percentage.compareTo(threshold) < 0) {
            return false;
        }

        log.warn("Alerta de orcamento: familia [{}] em {}% do teto da categoria [{}]",
                event.getFamilyId(), percentage, event.getCategoryName());

        eventPublisher.publish(KafkaTopics.BUDGETS, BudgetExceededEvent.builder()
                .budgetId(budget.getId())
                .familyId(event.getFamilyId())
                .categoryId(event.getCategoryId())
                .categoryName(event.getCategoryName())
                .plannedAmount(planned)
                .currentSpent(spent)
                .currentPercentage(percentage.doubleValue())
                .thresholdPercentage(threshold.doubleValue())
                .build());

        notificationRepository.save(new Notification(
                UUID.randomUUID(),
                event.getFamilyId(),
                event.getUserId(),
                "Alerta de Orçamento: " + (event.getCategoryName() != null ? event.getCategoryName() : "Categoria"),
                String.format("Você atingiu %s%% do teto planejado (R$ %s de R$ %s).",
                        percentage.toPlainString(), spent.toPlainString(), planned.toPlainString()),
                NotificationType.BUDGET_WARNING,
                "/budgets",
                false,
                null,
                LocalDateTime.now(),
                null,
                null,
                null));
        return true;
    }
}
