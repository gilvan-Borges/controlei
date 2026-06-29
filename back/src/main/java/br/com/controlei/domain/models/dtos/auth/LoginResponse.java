package br.com.controlei.domain.models.dtos.auth;

import br.com.controlei.domain.models.dtos.user.UserResponse;

public record LoginResponse(
        String accessToken,
        String tokenType,
        UserResponse user
) {
}
