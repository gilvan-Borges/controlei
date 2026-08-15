package br.com.controlei.domain.models.dtos.split;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ExpenseSplitShareResponse(
        UUID id,
        UUID expenseSplitId,
        UUID userId,
        String userName,
        BigDecimal shareAmount,
        boolean settled,
        LocalDateTime settledAt
) {
}
