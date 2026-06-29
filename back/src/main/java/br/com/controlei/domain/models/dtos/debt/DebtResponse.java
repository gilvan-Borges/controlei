package br.com.controlei.domain.models.dtos.debt;

import br.com.controlei.domain.models.enums.DebtStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record DebtResponse(
        UUID id,
        UUID familyId,
        UUID userId,
        UUID categoryId,
        String description,
        LocalDate purchaseDate,
        BigDecimal totalAmount,
        int installmentCount,
        BigDecimal installmentAmount,
        DebtStatus status,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
