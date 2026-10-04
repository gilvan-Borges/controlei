package br.com.controlei.domain.models.dtos.card;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateCreditCardRequest(
        UUID userId,

        @NotBlank(message = "Nome do cartao e obrigatorio")
        String name,

        String lastDigits,

        String brand,

        @Min(value = 1, message = "Dia de fechamento deve ser entre 1 e 31")
        @Max(value = 31, message = "Dia de fechamento deve ser entre 1 e 31")
        int closingDay,

        @Min(value = 1, message = "Dia de vencimento deve ser entre 1 e 31")
        @Max(value = 31, message = "Dia de vencimento deve ser entre 1 e 31")
        int dueDay,

        @NotNull(message = "Limite de credito e obrigatorio")
        @Positive(message = "Limite de credito deve ser maior que zero")
        BigDecimal creditLimit
) {
}
