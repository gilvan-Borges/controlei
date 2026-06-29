package br.com.controlei.domain.models.dtos.transaction;

import br.com.controlei.domain.models.enums.TransactionStatus;
import br.com.controlei.domain.models.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID familyId,
        UUID userId,
        UUID accountId,
        UUID categoryId,
        TransactionType type,
        String description,
        BigDecimal amount,
        LocalDate transactionDate,
        LocalDate dueDate,
        LocalDateTime paidAt,
        TransactionStatus status,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
