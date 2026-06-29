package br.com.controlei.infrastructure.configurations.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Provides the current authenticated user for audit purposes.
 * 
 * Currently returns "system" as the default user since JWT authentication
 * is not yet implemented. This will be updated in TASK-006 when JWT is added.
 * 
 * Future implementation should extract user ID from JWT token:
 * - Get Authentication from SecurityContext
 * - Extract user ID from principal/claims
 * - Return user ID as String
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

        return Optional.of(authentication.getName());
    }
}
