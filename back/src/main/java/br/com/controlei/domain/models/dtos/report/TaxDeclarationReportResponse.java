package br.com.controlei.domain.models.dtos.report;

import java.math.BigDecimal;
import java.util.List;

public record TaxDeclarationReportResponse(
        int year,
        String familyName,
        List<AccountTaxItem> accounts,
        List<InvestmentTaxItem> investments,
        List<DebtTaxItem> debts,
        BigDecimal totalAssets,
        BigDecimal totalLiabilities,
        BigDecimal netWorth
) {
}
