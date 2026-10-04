package br.com.controlei.domain.models.dtos.investment;

import java.math.BigDecimal;
import java.util.List;

public record InvestmentPortfolioSummaryResponse(
        BigDecimal totalPortfolioValue,
        int totalInvestmentsCount,
        List<InvestmentAssetClassSummary> assetClasses
) {
}
