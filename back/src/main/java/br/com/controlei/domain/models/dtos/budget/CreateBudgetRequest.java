package br.com.controlei.domain.models.dtos.budget;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateBudgetRequest(
        UUID userId,

        @NotNull(message = "Categoria e obrigatoria")
        UUID categoryId,

        @NotNull(message = "Ano e obrigatorio")
        @Min(value = 2000, message = "Ano invalido")
        @Max(value = 2100, message = "Ano invalido")
        Integer year,

        @NotNull(message = "Mes e obrigatorio")
        @Min(value = 1, message = "Mes deve ser entre 1 e 12")
        @Max(value = 12, message = "Mes deve ser entre 1 e 12")
        Integer month,

        @NotNull(message = "Valor planejado e obrigatorio")
        @Positive(message = "Valor planejado deve ser positivo")
        BigDecimal plannedAmount,

        @Min(value = 1, message = "Percentual de alerta deve ser no minimo 1%")
        @Max(value = 100, message = "Percentual de alerta deve ser no maximo 100%")
        Integer alertThresholdPercent
) {
}
