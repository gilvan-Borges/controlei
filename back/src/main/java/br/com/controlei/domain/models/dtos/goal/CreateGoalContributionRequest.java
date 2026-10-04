package br.com.controlei.domain.models.dtos.goal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateGoalContributionRequest(
        UUID accountId,

        @NotNull(message = "Valor do aporte e obrigatorio")
        @Positive(message = "Valor do aporte deve ser positivo")
        BigDecimal amount,

        LocalDate contributionDate,

        @Size(max = 500, message = "Observacoes devem ter no maximo 500 caracteres")
        String notes
) {
}
