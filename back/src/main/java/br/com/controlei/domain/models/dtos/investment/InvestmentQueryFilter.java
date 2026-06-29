package br.com.controlei.domain.models.dtos.investment;

import br.com.controlei.domain.models.enums.InvestmentType;

import java.time.LocalDate;
import java.util.UUID;

public record InvestmentQueryFilter(
        UUID userId,
        InvestmentType type,
        UUID categoryId,
        LocalDate startDate,
        LocalDate endDate
) {
}
