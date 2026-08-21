package br.com.controlei.shared.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetExceededEvent implements DomainEvent {
    @Builder.Default
    private UUID eventId = UUID.randomUUID();

    @Builder.Default
    private Instant occurredOn = Instant.now();

    @Builder.Default
    private String eventType = "BUDGET_THRESHOLD_EXCEEDED";

    private UUID budgetId;
    private UUID familyId;
    private UUID categoryId;
    private String categoryName;
    private BigDecimal plannedAmount;
    private BigDecimal currentSpent;
    private double currentPercentage;
    private double thresholdPercentage;
}
