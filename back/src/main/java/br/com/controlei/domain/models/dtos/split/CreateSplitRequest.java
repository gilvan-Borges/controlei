package br.com.controlei.domain.models.dtos.split;

import br.com.controlei.domain.models.enums.SplitType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateSplitRequest(
        @NotNull(message = "Transacao e obrigatoria")
        UUID transactionId,

        @NotNull(message = "Tipo de divisao e obrigatorio")
        SplitType splitType,

        @NotEmpty(message = "Lista de participantes e obrigatoria")
        List<SplitShareItemRequest> shares,

        String notes
) {
}
