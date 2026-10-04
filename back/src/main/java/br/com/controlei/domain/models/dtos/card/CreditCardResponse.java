package br.com.controlei.domain.models.dtos.card;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreditCardResponse(
        UUID id,
        UUID familyId,
        UUID userId,
        String userName,
        String name,
        String lastDigits,
        String brand,
        int closingDay,
        int dueDay,
        BigDecimal creditLimit,
        BigDecimal availableLimit,
        BigDecimal currentInvoiceAmount,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
