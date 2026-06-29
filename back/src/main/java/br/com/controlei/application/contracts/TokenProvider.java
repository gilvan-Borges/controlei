package br.com.controlei.application.contracts;

import br.com.controlei.domain.models.dtos.auth.AuthenticatedUser;

import java.util.Optional;

public interface TokenProvider {

    String generateToken(AuthenticatedUser user);

    Optional<AuthenticatedUser> validateToken(String token);
}
