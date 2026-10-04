package br.com.controlei.domain.models.dtos.split;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SettleDebtRequest(
        @NotNull(message = "Pagador e obrigatorio")
        UUID fromUserId,

        @NotNull(message = "Recebedor e obrigatorio")
        UUID toUserId,

        @NotNull(message = "Valor e obrigatorio")
        @Positive(message = "Valor deve ser positivo")
        BigDecimal amount,

        LocalDate settlementDate,

        String notes
) {
}
