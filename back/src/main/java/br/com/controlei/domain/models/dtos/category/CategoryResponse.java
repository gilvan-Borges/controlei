package br.com.controlei.domain.models.dtos.category;

import br.com.controlei.domain.models.enums.CategoryType;

import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        UUID familyId,
        String name,
        CategoryType type,
        String color,
        String icon,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
