package br.com.controlei.domain.models.dtos.family;

import java.time.LocalDateTime;
import java.util.UUID;

public record FamilyResponse(
        UUID id,
        String name,
        UUID responsibleUserId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
