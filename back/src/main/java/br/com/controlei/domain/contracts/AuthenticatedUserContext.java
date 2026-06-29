package br.com.controlei.domain.contracts;

import br.com.controlei.domain.models.dtos.auth.AuthenticatedUser;

import java.util.Optional;

public interface AuthenticatedUserContext {

    Optional<AuthenticatedUser> getCurrentUser();
}
