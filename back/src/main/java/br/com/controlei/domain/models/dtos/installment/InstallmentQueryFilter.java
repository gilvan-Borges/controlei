package br.com.controlei.domain.models.dtos.installment;

import br.com.controlei.domain.models.enums.InstallmentStatus;

import java.time.LocalDate;
import java.util.UUID;

public record InstallmentQueryFilter(
        UUID userId,
        UUID debtId,
        InstallmentStatus status,
        LocalDate startDate,
        LocalDate endDate
) {
}
