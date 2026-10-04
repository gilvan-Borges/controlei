package br.com.controlei.domain.models.dtos.debt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateDebtRequest(
        UUID categoryId,

        @NotBlank(message = "Descricao e obrigatoria")
        @Size(min = 2, max = 500, message = "Descricao deve ter entre 2 e 500 caracteres")
        String description,

        @NotNull(message = "Data da compra e obrigatoria")
        LocalDate purchaseDate,

        @Size(max = 2000, message = "Observacoes devem ter no maximo 2000 caracteres")
        String notes
) {
}
