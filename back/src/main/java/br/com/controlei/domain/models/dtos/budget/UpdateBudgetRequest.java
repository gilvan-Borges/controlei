package br.com.controlei.domain.models.dtos.budget;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateBudgetRequest(
        @NotNull(message = "Valor planejado e obrigatorio")
        @Positive(message = "Valor planejado deve ser positivo")
        BigDecimal plannedAmount,

        @Min(value = 1, message = "Percentual de alerta deve ser no minimo 1%")
        @Max(value = 100, message = "Percentual de alerta deve ser no maximo 100%")
        Integer alertThresholdPercent
) {
}
