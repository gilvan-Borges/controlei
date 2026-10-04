package br.com.controlei.domain.models.dtos.investment;

import br.com.controlei.domain.models.enums.InvestmentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record InvestmentResponse(
        UUID id,
        UUID familyId,
        UUID userId,
        UUID categoryId,
        String name,
        InvestmentType type,
        BigDecimal currentAmount,
        LocalDate referenceDate,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
