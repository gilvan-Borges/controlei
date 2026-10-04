package br.com.controlei.domain.models.dtos.dashboard;

import java.math.BigDecimal;
import java.util.List;

public record FamilyDashboardResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance,
        BigDecimal totalOpenDebts,
        BigDecimal totalPendingInstallments,
        BigDecimal totalInvested,
        List<UserDashboardDetail> userDetails
) {
}
