package br.com.controlei.domain.models.dtos.auth;

import br.com.controlei.domain.models.dtos.user.UserResponse;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
    public LoginResponse(String accessToken, String tokenType, UserResponse user) {
        this(accessToken, null, tokenType, 900L, user);
    }
}
