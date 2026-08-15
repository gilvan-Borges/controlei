package br.com.controlei.domain.models.dtos.card;

import br.com.controlei.domain.models.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        UUID creditCardId,
        String creditCardName,
        UUID familyId,
        UUID userId,
        String userName,
        LocalDate referenceMonth,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        InvoiceStatus status,
        LocalDate dueDate,
        LocalDateTime paidAt,
        List<CreditCardTransactionResponse> transactions,
        LocalDateTime createdAt
) {
}
