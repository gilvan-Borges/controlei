package br.com.controlei.domain.models.dtos.notification;

import br.com.controlei.domain.models.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID familyId,
        UUID userId,
        String title,
        String message,
        NotificationType type,
        String linkUrl,
        boolean read,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
}
