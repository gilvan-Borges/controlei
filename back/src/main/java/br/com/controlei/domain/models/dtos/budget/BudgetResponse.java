package br.com.controlei.domain.models.dtos.budget;

import br.com.controlei.domain.models.enums.BudgetStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BudgetResponse(
        UUID id,
        UUID familyId,
        UUID userId,
        String userName,
        UUID categoryId,
        String categoryName,
        String categoryColor,
        String categoryIcon,
        int year,
        int month,
        BigDecimal plannedAmount,
        BigDecimal spentAmount,
        BigDecimal remainingAmount,
        BigDecimal percentageUsed,
        int alertThresholdPercent,
        BudgetStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
