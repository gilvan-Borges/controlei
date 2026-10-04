package br.com.controlei.domain.models.dtos.card;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreditCardTransactionResponse(
        UUID id,
        UUID creditCardId,
        UUID invoiceId,
        UUID familyId,
        UUID userId,
        UUID categoryId,
        String categoryName,
        String description,
        BigDecimal amount,
        LocalDate transactionDate,
        int installmentNumber,
        int totalInstallments,
        LocalDateTime createdAt
) {
}
