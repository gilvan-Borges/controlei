package br.com.controlei.domain.models.dtos.investment;

import br.com.controlei.domain.models.enums.InvestmentTransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record InvestmentTransactionResponse(
        UUID id,
        UUID investmentId,
        UUID familyId,
        UUID userId,
        String userName,
        UUID accountId,
        String accountName,
        InvestmentTransactionType type,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        LocalDate transactionDate,
        String notes,
        LocalDateTime createdAt
) {
}
