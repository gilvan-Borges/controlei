package br.com.controlei.infrastructure.configurations.audit;

import br.com.controlei.domain.models.dtos.auth.AuthenticatedUser;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Provides the current authenticated user for audit purposes.
 * Extracts the user ID from the JWT authentication context.
 */
@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    private static final String SYSTEM_USER = "system";

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.of(SYSTEM_USER);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUser user) {
            return Optional.of(user.userId().toString());
        }

        return Optional.of(authentication.getName());
    }
}
