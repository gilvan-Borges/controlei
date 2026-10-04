package br.com.controlei.domain.models.dtos.card;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PayInvoiceRequest(
        @NotNull(message = "Conta de debito e obrigatoria")
        UUID accountId,

        @Positive(message = "Valor de pagamento deve ser positivo")
        BigDecimal amount,

        LocalDate paymentDate
) {
}
