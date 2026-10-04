package br.com.controlei.domain.models.dtos.receipt;

import jakarta.validation.constraints.NotBlank;

public record SimulateScanRequest(
        @NotBlank(message = "Texto do comprovante obrigatorio")
        String receiptText
) {
}
