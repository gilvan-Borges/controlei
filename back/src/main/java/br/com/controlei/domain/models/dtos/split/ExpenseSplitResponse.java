package br.com.controlei.domain.models.dtos.split;

import br.com.controlei.domain.models.enums.SplitType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ExpenseSplitResponse(
        UUID id,
        UUID transactionId,
        UUID familyId,
        UUID paidByUserId,
        String paidByUserName,
        SplitType splitType,
        BigDecimal totalAmount,
        String notes,
        List<ExpenseSplitShareResponse> shares,
        LocalDateTime createdAt
) {
}
