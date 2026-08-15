package br.com.controlei.domain.models.dtos.split;

import java.math.BigDecimal;
import java.util.UUID;

public record SuggestedSettlement(
        UUID fromUserId,
        String fromUserName,
        UUID toUserId,
        String toUserName,
        BigDecimal amount
) {
}
