package br.com.controlei.domain.models.dtos.recurring;

import br.com.controlei.domain.models.enums.RecurrenceFrequency;
import br.com.controlei.domain.models.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RecurringTransactionResponse(
        UUID id,
        UUID familyId,
        UUID userId,
        String userName,
        UUID accountId,
        String accountName,
        UUID categoryId,
        String categoryName,
        TransactionType type,
        String description,
        BigDecimal amount,
        RecurrenceFrequency frequency,
        Integer dayOfMonth,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate nextExecutionDate,
        boolean autoPay,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
