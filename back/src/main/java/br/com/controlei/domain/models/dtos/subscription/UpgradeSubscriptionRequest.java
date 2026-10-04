package br.com.controlei.domain.models.dtos.subscription;

import br.com.controlei.domain.models.enums.PlanType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpgradeSubscriptionRequest(
        @NotNull(message = "O tipo do plano e obrigatorio")
        PlanType planType,

        @Min(value = 0, message = "Quantidade de membros extras nao pode ser negativa")
        @Max(value = 50, message = "Maximo de 50 membros extras")
        int extraMembers,

        String paymentMethod
) {}
