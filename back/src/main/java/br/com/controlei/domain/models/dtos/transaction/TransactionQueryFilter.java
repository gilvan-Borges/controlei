package br.com.controlei.domain.models.dtos.transaction;

import br.com.controlei.domain.models.enums.TransactionStatus;
import br.com.controlei.domain.models.enums.TransactionType;

import java.time.LocalDate;
import java.util.UUID;

public record TransactionQueryFilter(
        LocalDate startDate,
        LocalDate endDate,
        UUID userId,
        UUID accountId,
        UUID categoryId,
        TransactionType type,
        TransactionStatus status
) {
}
