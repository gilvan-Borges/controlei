package br.com.controlei.domain.models.dtos.user;

import br.com.controlei.domain.models.enums.Role;

import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        UUID familyId,
        String name,
        String email,
        Role role,
        boolean active
) {
}
