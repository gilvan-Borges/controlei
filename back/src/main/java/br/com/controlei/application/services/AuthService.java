package br.com.controlei.application.services;

import br.com.controlei.application.contracts.TokenProvider;
import br.com.controlei.application.exceptions.BusinessException;
import br.com.controlei.application.exceptions.UnauthorizedException;
import br.com.controlei.application.mappers.UserMapper;
import br.com.controlei.domain.contracts.PasswordHasher;
import br.com.controlei.domain.contracts.repositories.FamilyRepositoryPort;
import br.com.controlei.domain.contracts.repositories.RefreshTokenRepositoryPort;
import br.com.controlei.domain.contracts.repositories.UserRepositoryPort;
import br.com.controlei.domain.models.dtos.auth.AuthenticatedUser;
import br.com.controlei.domain.models.dtos.auth.LoginRequest;
import br.com.controlei.domain.models.dtos.auth.LoginResponse;
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

    public AuthService(UserRepositoryPort userRepository,
                       FamilyRepositoryPort familyRepository,
                       RefreshTokenRepositoryPort refreshTokenRepository,
                       UserMapper userMapper,
                       PasswordHasher passwordHasher,
                       TokenProvider tokenProvider,
                       @Value("${jwt.refresh-expiration-days:7}") long refreshTokenExpirationDays) {
        this.userRepository = userRepository;
        this.familyRepository = familyRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userMapper = userMapper;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
    }

    @Transactional
    public LoginResponse registerFamily(RegisterFamilyRequest request) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
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
                request.email(),
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

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                savedResponsible.getId(),
                savedFamily.getId(),
                savedResponsible.getEmail(),
                savedResponsible.getRole()
        );

        String accessToken = tokenProvider.generateToken(authenticatedUser);
        RefreshToken refreshToken = createRefreshToken(savedResponsible.getId());
        UserResponse userResponse = userMapper.toResponse(savedResponsible);

        return new LoginResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                tokenProvider.getExpirationSeconds(),
                userResponse
        );
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new UnauthorizedException("Credenciais invalidas"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Credenciais invalidas");
        }

        if (!passwordHasher.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Credenciais invalidas");
        }

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                user.getId(),
                user.getFamilyId(),
                user.getEmail(),
                user.getRole()
        );

        String accessToken = tokenProvider.generateToken(authenticatedUser);
        RefreshToken refreshToken = createRefreshToken(user.getId());
        UserResponse userResponse = userMapper.toResponse(user);

        return new LoginResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                tokenProvider.getExpirationSeconds(),
                userResponse
        );
    }

    @Transactional
    public LoginResponse refreshToken(String tokenValue) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenValue)
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

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                user.getId(),
                user.getFamilyId(),
                user.getEmail(),
                user.getRole()
        );

        String newAccessToken = tokenProvider.generateToken(authenticatedUser);
        RefreshToken newRefreshToken = createRefreshToken(user.getId());
        UserResponse userResponse = userMapper.toResponse(user);

        return new LoginResponse(
                newAccessToken,
                newRefreshToken.getToken(),
                "Bearer",
                tokenProvider.getExpirationSeconds(),
                userResponse
        );
    }

    @Transactional
    public void logout(String tokenValue) {
        if (tokenValue != null && !tokenValue.isBlank()) {
            refreshTokenRepository.findByToken(tokenValue).ifPresent(t -> {
                t.setRevoked(true);
                refreshTokenRepository.save(t);
            });
        }
    }

    private RefreshToken createRefreshToken(UUID userId) {
        RefreshToken refreshToken = new RefreshToken(
                UUID.randomUUID(),
                userId,
                UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", ""),
                LocalDateTime.now().plusDays(refreshTokenExpirationDays),
                false,
                LocalDateTime.now()
        );
        return refreshTokenRepository.save(refreshToken);
    }
}
