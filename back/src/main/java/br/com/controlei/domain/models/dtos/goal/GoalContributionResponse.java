package br.com.controlei.domain.models.dtos.goal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record GoalContributionResponse(
        UUID id,
        UUID goalId,
        UUID familyId,
        UUID userId,
        String userName,
        UUID accountId,
        String accountName,
        BigDecimal amount,
        LocalDate contributionDate,
        String notes,
        LocalDateTime createdAt
) {
}
