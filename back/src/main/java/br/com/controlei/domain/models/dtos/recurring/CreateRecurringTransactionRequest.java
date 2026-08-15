package br.com.controlei.domain.models.dtos.recurring;

import br.com.controlei.domain.models.enums.RecurrenceFrequency;
import br.com.controlei.domain.models.enums.TransactionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateRecurringTransactionRequest(
        UUID userId,

        @NotNull(message = "Conta e obrigatoria")
        UUID accountId,

        UUID categoryId,

        @NotNull(message = "Tipo de transacao e obrigatorio")
        TransactionType type,

        @NotBlank(message = "Descricao e obrigatoria")
        String description,

        @NotNull(message = "Valor e obrigatorio")
        @Positive(message = "Valor deve ser positivo")
        BigDecimal amount,

        @NotNull(message = "Frequencia e obrigatoria")
        RecurrenceFrequency frequency,

        @Min(value = 1, message = "Dia do mes deve ser entre 1 e 31")
        @Max(value = 31, message = "Dia do mes deve ser entre 1 e 31")
        Integer dayOfMonth,

        @NotNull(message = "Data de inicio e obrigatoria")
        LocalDate startDate,

        LocalDate endDate,

        Boolean autoPay
) {
}
