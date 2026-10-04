package br.com.controlei.domain.models.dtos.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "Refresh token e obrigatorio")
        String refreshToken
) {
}
