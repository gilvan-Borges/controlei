package br.com.controlei.domain.models.dtos.auth;

import br.com.controlei.domain.models.enums.Role;

import java.util.UUID;

public record AuthenticatedUser(
        UUID userId,
        UUID familyId,
        String email,
        Role role
) {
}
