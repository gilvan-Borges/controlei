package br.com.controlei.domain.models.dtos.dashboard;

import java.math.BigDecimal;
import java.util.UUID;

public record UserDashboardDetail(
        UUID userId,
        String userName,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal balance,
        BigDecimal openDebts,
        BigDecimal pendingInstallments,
        BigDecimal investments
) {
}
