package br.com.controlei.domain.models.dtos.split;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record SplitShareItemRequest(
        @NotNull(message = "Usuario e obrigatorio")
        UUID userId,

        @Positive(message = "Valor ou percentual deve ser positivo")
        BigDecimal amountOrPercentage
) {
}
