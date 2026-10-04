package br.com.controlei.domain.models.dtos.goal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record WithdrawGoalRequest(
        @NotNull(message = "Conta de destino e obrigatoria")
        UUID accountId,

        @NotNull(message = "Valor do resgate e obrigatorio")
        @Positive(message = "Valor do resgate deve ser positivo")
        BigDecimal amount,

        String notes
) {
}
