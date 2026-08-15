package br.com.controlei.domain.models.dtos.investment;

import br.com.controlei.domain.models.enums.InvestmentType;

import java.math.BigDecimal;

public record InvestmentAssetClassSummary(
        InvestmentType type,
        BigDecimal totalAmount,
        BigDecimal percentage,
        int count
) {
}
