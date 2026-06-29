package br.com.controlei.domain.models.dtos.installment;

import br.com.controlei.domain.models.dtos.debt.DebtResponse;

public record PayInstallmentResponse(
        InstallmentResponse installment,
        DebtResponse debt
) {
}
