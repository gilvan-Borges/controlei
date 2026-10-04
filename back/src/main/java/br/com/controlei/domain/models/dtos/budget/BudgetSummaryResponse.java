package br.com.controlei.domain.models.dtos.budget;

import java.math.BigDecimal;
import java.util.List;

public record BudgetSummaryResponse(
        int year,
        int month,
        BigDecimal totalPlanned,
        BigDecimal totalSpent,
        BigDecimal totalRemaining,
        BigDecimal overallPercentageUsed,
        List<BudgetResponse> items
) {
}
