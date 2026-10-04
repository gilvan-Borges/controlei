package br.com.controlei.domain.models.dtos.receipt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SimulateScanRequest(
        @NotBlank(message = "Texto do comprovante obrigatorio")
        @Size(max = 20000, message = "Texto do comprovante excede 20000 caracteres")
        String receiptText
) {
}
