package br.com.controlei.domain.models.dtos.goal;

import br.com.controlei.domain.models.enums.GoalCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateGoalRequest(
        @NotBlank(message = "Nome e obrigatorio")
        @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
        String name,

        String description,

        @NotNull(message = "Valor alvo e obrigatorio")
        @Positive(message = "Valor alvo deve ser positivo")
        BigDecimal targetAmount,

        LocalDate targetDate,

        GoalCategory category
) {
}
