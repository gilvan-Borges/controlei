package br.com.controlei.application.services;

import br.com.controlei.application.exceptions.BusinessException;
import br.com.controlei.application.exceptions.UnauthorizedException;
import br.com.controlei.application.contracts.TokenProvider;
import br.com.controlei.application.mappers.UserMapper;
import br.com.controlei.domain.contracts.PasswordHasher;
import br.com.controlei.domain.contracts.repositories.FamilyRepositoryPort;
import br.com.controlei.domain.contracts.repositories.UserRepositoryPort;
import br.com.controlei.domain.models.dtos.auth.AuthenticatedUser;
import br.com.controlei.domain.models.dtos.auth.LoginRequest;
import br.com.controlei.domain.models.dtos.auth.LoginResponse;
import br.com.controlei.domain.models.dtos.auth.RegisterFamilyRequest;
import br.com.controlei.domain.models.dtos.user.UserResponse;
import br.com.controlei.domain.models.entities.Family;
import br.com.controlei.domain.models.entities.User;
import br.com.controlei.domain.models.enums.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepositoryPort userRepository;
    private final FamilyRepositoryPort familyRepository;
    private final UserMapper userMapper;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    public AuthService(UserRepositoryPort userRepository,
                       FamilyRepositoryPort familyRepository,
                       UserMapper userMapper,
                       PasswordHasher passwordHasher,
                       TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.familyRepository = familyRepository;
        this.userMapper = userMapper;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
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

        String token = tokenProvider.generateToken(authenticatedUser);
        UserResponse userResponse = userMapper.toResponse(savedResponsible);

        return new LoginResponse(token, "Bearer", userResponse);
    }

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

        String token = tokenProvider.generateToken(authenticatedUser);
        UserResponse userResponse = userMapper.toResponse(user);

        return new LoginResponse(token, "Bearer", userResponse);
    }
}
