package br.com.controlei.domain.models.dtos.investment;

import br.com.controlei.domain.models.enums.InvestmentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateInvestmentRequest(
        UUID categoryId,

        @NotBlank(message = "Nome e obrigatorio")
        @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
        String name,

        @NotNull(message = "Tipo e obrigatorio")
        InvestmentType type,

        @NotNull(message = "Valor atual e obrigatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "Valor atual deve ser maior ou igual a zero")
        BigDecimal currentAmount,

        LocalDate referenceDate,

        @Size(max = 2000, message = "Observacoes devem ter no maximo 2000 caracteres")
        String notes
) {
}
