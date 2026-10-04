package br.com.controlei.domain.models.dtos.goal;

import br.com.controlei.domain.models.enums.GoalCategory;
import br.com.controlei.domain.models.enums.GoalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record FinancialGoalResponse(
        UUID id,
        UUID familyId,
        UUID userId,
        String userName,
        String name,
        String description,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        BigDecimal remainingAmount,
        BigDecimal progressPercentage,
        LocalDate targetDate,
        GoalCategory category,
        GoalStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
