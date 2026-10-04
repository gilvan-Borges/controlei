package br.com.controlei.domain.models.dtos.account;

import br.com.controlei.domain.models.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateAccountRequest(
        @NotBlank(message = "Nome e obrigatorio")
        @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
        String name,

        @NotNull(message = "Tipo e obrigatorio")
        AccountType type,

        @NotNull(message = "Compartilhada e obrigatoria")
        Boolean shared,

        UUID userId,

        @NotNull(message = "Saldo inicial e obrigatorio")
        @PositiveOrZero(message = "Saldo inicial deve ser maior ou igual a zero")
        BigDecimal initialBalance,

        Boolean active
) {
}
