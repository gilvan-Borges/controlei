package br.com.controlei.domain.models.dtos.investment;

import br.com.controlei.domain.models.enums.InvestmentTransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateInvestmentTransactionRequest(
        UUID accountId,

        @NotNull(message = "Tipo de movimentacao e obrigatorio")
        InvestmentTransactionType type,

        BigDecimal quantity,

        BigDecimal unitPrice,

        @NotNull(message = "Valor total e obrigatorio")
        @Positive(message = "Valor total deve ser positivo")
        BigDecimal totalAmount,

        LocalDate transactionDate,

        String notes
) {
}
