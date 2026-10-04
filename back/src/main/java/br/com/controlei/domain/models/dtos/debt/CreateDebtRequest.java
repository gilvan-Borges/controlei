package br.com.controlei.domain.models.dtos.debt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateDebtRequest(
        @NotNull(message = "Usuario e obrigatorio")
        UUID userId,

        UUID categoryId,

        @NotBlank(message = "Descricao e obrigatoria")
        @Size(min = 2, max = 500, message = "Descricao deve ter entre 2 e 500 caracteres")
        String description,

        @NotNull(message = "Data da compra e obrigatoria")
        LocalDate purchaseDate,

        @NotNull(message = "Valor total e obrigatorio")
        @Positive(message = "Valor total deve ser maior que zero")
        BigDecimal totalAmount,

        @NotNull(message = "Quantidade de parcelas e obrigatoria")
        @Positive(message = "Quantidade de parcelas deve ser maior que zero")
        Integer installmentCount,

        @NotNull(message = "Primeira data de vencimento e obrigatoria")
        LocalDate firstDueDate,

        @Size(max = 2000, message = "Observacoes devem ter no maximo 2000 caracteres")
        String notes
) {
}
