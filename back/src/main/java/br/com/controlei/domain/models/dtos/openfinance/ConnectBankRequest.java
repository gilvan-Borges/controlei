package br.com.controlei.domain.models.dtos.openfinance;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record ConnectBankRequest(
        @NotBlank(message = "ID da instituicao obrigatorio")
        String institutionId,

        @NotBlank(message = "Nome da instituicao obrigatorio")
        String institutionName,

        @NotBlank(message = "Item ID do Open Finance obrigatorio")
        String externalItemId,

        UUID targetAccountId,

        UUID targetCreditCardId
) {
}
