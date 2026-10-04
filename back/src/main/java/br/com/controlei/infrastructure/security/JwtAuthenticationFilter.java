package br.com.controlei.infrastructure.security;

import br.com.controlei.application.contracts.TokenProvider;
import br.com.controlei.domain.contracts.repositories.UserRepositoryPort;
import br.com.controlei.domain.models.dtos.auth.AuthenticatedUser;
import br.com.controlei.domain.models.entities.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final UserRepositoryPort userRepository;

    public JwtAuthenticationFilter(TokenProvider tokenProvider, UserRepositoryPort userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        extractToken(request)
                .flatMap(tokenProvider::validateToken)
                .flatMap(this::validateUserStillValid)
                .ifPresent(this::authenticate);

        filterChain.doFilter(request, response);
    }

    private Optional<AuthenticatedUser> validateUserStillValid(AuthenticatedUser user) {
        Optional<User> currentUser = userRepository.findByIdAndDeletedAtIsNull(user.userId());

        if (currentUser.isEmpty() || !currentUser.get().isActive()) {
            return Optional.empty();
        }

        if (!currentUser.get().getFamilyId().equals(user.familyId())
                || currentUser.get().getRole() != user.role()) {
            return Optional.empty();
        }

        return Optional.of(user);
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return Optional.of(header.substring(7));
        }
        return Optional.empty();
    }

    private void authenticate(AuthenticatedUser user) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
