package br.com.controlei.domain.models.dtos.dashboard;

import java.math.BigDecimal;
import java.util.List;

public record IndividualDashboardResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance,
        BigDecimal totalOpenDebts,
        BigDecimal totalPendingInstallments,
        List<UpcomingInstallment> upcomingInstallments,
        BigDecimal totalInvested
) {
}
