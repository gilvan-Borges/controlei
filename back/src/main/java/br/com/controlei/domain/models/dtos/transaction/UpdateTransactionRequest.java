package br.com.controlei.domain.models.dtos.transaction;

import br.com.controlei.domain.models.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateTransactionRequest(
        @NotNull(message = "Usuario e obrigatorio")
        UUID userId,

        @NotNull(message = "Conta e obrigatoria")
        UUID accountId,

        UUID categoryId,

        @NotNull(message = "Tipo e obrigatorio")
        TransactionType type,

        @NotBlank(message = "Descricao e obrigatoria")
        @Size(min = 2, max = 500, message = "Descricao deve ter entre 2 e 500 caracteres")
        String description,

        @NotNull(message = "Valor e obrigatorio")
        @Positive(message = "Valor deve ser maior que zero")
        BigDecimal amount,

        @NotNull(message = "Data da transacao e obrigatoria")
        LocalDate transactionDate,

        LocalDate dueDate,

        @Size(max = 2000, message = "Observacoes devem ter no maximo 2000 caracteres")
        String notes
) {
}
