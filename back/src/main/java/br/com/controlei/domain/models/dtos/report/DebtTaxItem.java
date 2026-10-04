package br.com.controlei.domain.models.dtos.report;

import java.math.BigDecimal;

public record DebtTaxItem(
        String debtName,
        BigDecimal totalAmount,
        BigDecimal remainingAmount
) {
}
