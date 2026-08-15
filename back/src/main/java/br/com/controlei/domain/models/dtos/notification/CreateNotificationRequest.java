package br.com.controlei.domain.models.dtos.notification;

import br.com.controlei.domain.models.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateNotificationRequest(
        UUID userId,

        @NotBlank(message = "Titulo e obrigatorio")
        String title,

        @NotBlank(message = "Mensagem e obrigatoria")
        String message,

        @NotNull(message = "Tipo e obrigatorio")
        NotificationType type,

        String linkUrl
) {
}
