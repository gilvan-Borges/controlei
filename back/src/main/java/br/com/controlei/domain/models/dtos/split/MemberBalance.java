package br.com.controlei.domain.models.dtos.split;

import java.math.BigDecimal;
import java.util.UUID;

public record MemberBalance(
        UUID userId,
        String userName,
        BigDecimal totalPaid,
        BigDecimal totalOwed,
        BigDecimal netBalance
) {
}
