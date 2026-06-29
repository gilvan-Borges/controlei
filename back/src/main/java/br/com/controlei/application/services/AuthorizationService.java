package br.com.controlei.application.services;

import br.com.controlei.domain.contracts.AuthenticatedUserContext;
import br.com.controlei.domain.models.dtos.auth.AuthenticatedUser;
import br.com.controlei.domain.models.enums.Role;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class AuthorizationService {

    private final AuthenticatedUserContext authenticatedUserContext;

    public AuthorizationService(AuthenticatedUserContext authenticatedUserContext) {
        this.authenticatedUserContext = authenticatedUserContext;
    }

    public AuthenticatedUser requireCurrentUser() {
        return authenticatedUserContext.getCurrentUser()
                .orElseThrow(() -> new br.com.controlei.application.exceptions.UnauthorizedException("Usuario nao autenticado"));
    }

    public void requireSameFamily(UUID resourceFamilyId) {
        AuthenticatedUser current = requireCurrentUser();
        if (!Objects.equals(current.familyId(), resourceFamilyId)) {
            throw new br.com.controlei.application.exceptions.NotFoundException("Recurso nao encontrado");
        }
    }

    public void requireCanWrite(UUID resourceFamilyId, UUID resourceUserId) {
        requireSameFamily(resourceFamilyId);
        AuthenticatedUser current = requireCurrentUser();

        if (current.role() == Role.RESPONSIBLE) {
            return;
        }

        if (!Objects.equals(current.userId(), resourceUserId)) {
            throw new br.com.controlei.application.exceptions.ForbiddenException("Acesso negado");
        }
    }

    public boolean isResponsible() {
        return authenticatedUserContext.getCurrentUser()
                .map(user -> user.role() == Role.RESPONSIBLE)
                .orElse(false);
    }

    public UUID currentUserId() {
        return requireCurrentUser().userId();
    }

    public UUID currentFamilyId() {
        return requireCurrentUser().familyId();
    }
}
