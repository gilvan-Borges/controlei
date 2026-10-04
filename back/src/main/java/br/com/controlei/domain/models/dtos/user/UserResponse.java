package br.com.controlei.domain.models.dtos.user;

import br.com.controlei.domain.models.enums.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        UUID familyId,
        String name,
        String email,
        Role role,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
