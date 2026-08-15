package br.com.controlei.domain.models.dtos.report;

import java.math.BigDecimal;

public record InvestmentTaxItem(
        String investmentName,
        String investmentType,
        BigDecimal currentAmount
) {
}
