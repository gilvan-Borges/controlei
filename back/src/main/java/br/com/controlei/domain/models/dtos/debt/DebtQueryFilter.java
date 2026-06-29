package br.com.controlei.domain.models.dtos.debt;

import br.com.controlei.domain.models.enums.DebtStatus;

import java.util.UUID;

public record DebtQueryFilter(
        UUID userId,
        DebtStatus status
) {
}
