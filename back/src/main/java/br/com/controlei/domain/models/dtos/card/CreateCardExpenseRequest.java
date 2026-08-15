package br.com.controlei.domain.models.dtos.card;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateCardExpenseRequest(
        @NotBlank(message = "Descricao e obrigatoria")
        String description,

        @NotNull(message = "Valor e obrigatorio")
        @Positive(message = "Valor deve ser positivo")
        BigDecimal amount,

        @NotNull(message = "Data da transacao e obrigatoria")
        LocalDate transactionDate,

        UUID categoryId,

        @Min(value = 1, message = "Numero de parcelas deve ser no minimo 1")
        Integer totalInstallments
) {
}
