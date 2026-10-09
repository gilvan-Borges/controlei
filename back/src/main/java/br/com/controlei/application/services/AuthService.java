package br.com.controlei.application.services;

import br.com.controlei.application.contracts.TokenProvider;
import br.com.controlei.application.exceptions.BusinessException;
import br.com.controlei.application.exceptions.ForbiddenException;
import br.com.controlei.application.exceptions.UnauthorizedException;
import br.com.controlei.application.security.LoginAttemptTracker;
import br.com.controlei.application.security.TokenHasher;
import br.com.controlei.application.mappers.UserMapper;
import br.com.controlei.domain.contracts.PasswordHasher;
import br.com.controlei.domain.contracts.repositories.FamilyRepositoryPort;
import br.com.controlei.domain.contracts.repositories.RefreshTokenRepositoryPort;
import br.com.controlei.domain.contracts.repositories.UserRepositoryPort;
import br.com.controlei.domain.models.dtos.auth.AuthenticatedUser;
import br.com.controlei.domain.models.dtos.auth.LoginRequest;
import br.com.controlei.domain.models.dtos.auth.LoginResponse;
import br.com.controlei.domain.models.dtos.auth.PublicAuthConfig;
import br.com.controlei.domain.models.dtos.auth.RegisterFamilyRequest;
import br.com.controlei.domain.models.dtos.user.UserResponse;
import br.com.controlei.domain.models.entities.Family;
import br.com.controlei.domain.models.entities.RefreshToken;
import br.com.controlei.domain.models.entities.User;
import br.com.controlei.domain.models.enums.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepositoryPort userRepository;
    private final FamilyRepositoryPort familyRepository;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final UserMapper userMapper;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;
    private final long refreshTokenExpirationDays;
    private final LoginAttemptTracker loginAttempts;
    private final boolean registrationEnabled;
    private final FamilyDefaultsService familyDefaults;
    private final DemoService demo;
    /** Hash de uma senha qualquer, para gastar o mesmo tempo de BCrypt quando o e-mail nao existe. */
    private final String dummyHash;

    public AuthService(UserRepositoryPort userRepository,
                       FamilyRepositoryPort familyRepository,
                       RefreshTokenRepositoryPort refreshTokenRepository,
                       UserMapper userMapper,
                       PasswordHasher passwordHasher,
                       TokenProvider tokenProvider,
                       LoginAttemptTracker loginAttempts,
                       @Value("${app.registration.enabled:true}") boolean registrationEnabled,
                       FamilyDefaultsService familyDefaults,
                       DemoService demo,
                       @Value("${jwt.refresh-expiration-days:7}") long refreshTokenExpirationDays) {
        this.userRepository = userRepository;
        this.familyRepository = familyRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userMapper = userMapper;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
        this.loginAttempts = loginAttempts;
        this.registrationEnabled = registrationEnabled;
        this.familyDefaults = familyDefaults;
        this.demo = demo;
        this.dummyHash = passwordHasher.hash("senha-ficticia-para-igualar-o-tempo");
    }

    private static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional
    public LoginResponse registerFamily(RegisterFamilyRequest request) {
        if (!registrationEnabled) {
            throw new ForbiddenException("O cadastro de novas familias esta fechado");
        }
        String email = normalize(request.email());
        if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new BusinessException("Email ja cadastrado");
        }

        Family family = new Family(
                UUID.randomUUID(),
                request.familyName(),
                null,
                LocalDateTime.now(),
                null,
                null,
                null
        );
        Family savedFamily = familyRepository.save(family);

        User responsible = new User(
                UUID.randomUUID(),
                savedFamily.getId(),
                request.responsibleName(),
                email,
                passwordHasher.hash(request.password()),
                Role.RESPONSIBLE,
                true,
                LocalDateTime.now(),
                null,
                null,
                null
        );
        User savedResponsible = userRepository.save(responsible);

        savedFamily.setResponsibleUserId(savedResponsible.getId());
        familyRepository.save(savedFamily);

        // Categorias comuns e uma conta "Carteira": a familia ja pode lancar a primeira despesa
        familyDefaults.createFor(savedFamily.getId(), savedResponsible.getId());

        return sessionFor(savedResponsible);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = normalize(request.email());
        if (loginAttempts.isLocked(email)) {
            throw new UnauthorizedException("Muitas tentativas. Tente novamente em alguns minutos.");
        }

        User user = userRepository.findByEmailAndDeletedAtIsNull(email).orElse(null);
        boolean usable = user != null && user.isActive();
        // Sempre roda o BCrypt (contra um hash qualquer se a conta nao existe): o tempo de resposta nao revela
        // se o e-mail esta cadastrado.
        boolean passwordOk = passwordHasher.matches(request.password(), usable ? user.getPasswordHash() : dummyHash);
        if (!usable || !passwordOk) {
            loginAttempts.recordFailure(email);
            throw new UnauthorizedException("Credenciais invalidas");
        }
        loginAttempts.recordSuccess(email);
        return sessionFor(user);
    }

    /** Entrada sem senha na familia de demonstracao. Com o modo desligado, responde 404 como rota inexistente. */
    @Transactional
    public LoginResponse loginAsDemoVisitor() {
        return sessionFor(demo.visitor());
    }

    /** O que a tela de login precisa saber antes de qualquer login. */
    public PublicAuthConfig publicConfig() {
        return new PublicAuthConfig(registrationEnabled, demo.isEnabled());
    }

    @Transactional
    public LoginResponse refreshToken(String tokenValue) {
        RefreshToken token = refreshTokenRepository.findByToken(TokenHasher.sha256(tokenValue))
                .orElseThrow(() -> new UnauthorizedException("Token de atualizacao invalido ou expirado"));

        if (token.isRevoked()) {
            // Possível reuso fraudulento de token: revoga todos os tokens do usuário por segurança
            refreshTokenRepository.revokeAllByUserId(token.getUserId());
            throw new UnauthorizedException("Token de atualizacao revogado. Faca login novamente.");
        }

        if (token.isExpired()) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            throw new UnauthorizedException("Token de atualizacao expirado. Faca login novamente.");
        }

        // Rotação de Refresh Token: revoga o anterior
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        User user = userRepository.findByIdAndDeletedAtIsNull(token.getUserId())
                .orElseThrow(() -> new UnauthorizedException("Usuario nao encontrado"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Usuario inativo");
        }

        return sessionFor(user);
    }

    @Transactional
    public void logout(String tokenValue) {
        if (tokenValue != null && !tokenValue.isBlank()) {
            refreshTokenRepository.findByToken(TokenHasher.sha256(tokenValue)).ifPresent(t -> {
                t.setRevoked(true);
                refreshTokenRepository.save(t);
            });
        }
    }

    private LoginResponse sessionFor(User user) {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                user.getId(),
                user.getFamilyId(),
                user.getEmail(),
                user.getRole()
        );
        UserResponse userResponse = userMapper.toResponse(user);
        return new LoginResponse(
                tokenProvider.generateToken(authenticatedUser),
                createRefreshToken(user.getId()),
                "Bearer",
                tokenProvider.getExpirationSeconds(),
                userResponse
        );
    }

    /** Devolve o token em claro (vai so para o cliente); no banco fica apenas o hash. */
    private String createRefreshToken(UUID userId) {
        String raw = TokenHasher.newToken();
        refreshTokenRepository.save(new RefreshToken(
                UUID.randomUUID(),
                userId,
                TokenHasher.sha256(raw),
                LocalDateTime.now().plusDays(refreshTokenExpirationDays),
                false,
                LocalDateTime.now()
        ));
        return raw;
    }
}
