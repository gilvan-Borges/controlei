package br.com.controlei.domain.models.dtos.report;

import java.math.BigDecimal;

public record AccountTaxItem(
        String accountName,
        String accountType,
        BigDecimal balance
) {
}
