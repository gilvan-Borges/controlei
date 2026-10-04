package br.com.controlei.domain.models.dtos.split;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SplitSettlementResponse(
        UUID id,
        UUID familyId,
        UUID fromUserId,
        String fromUserName,
        UUID toUserId,
        String toUserName,
        BigDecimal amount,
        LocalDate settlementDate,
        String notes,
        LocalDateTime createdAt
) {
}
