package br.com.controlei.application.services;

import br.com.controlei.application.exceptions.BusinessException;
import br.com.controlei.application.exceptions.ForbiddenException;
import br.com.controlei.application.exceptions.NotFoundException;
import br.com.controlei.application.mappers.UserMapper;
import br.com.controlei.domain.contracts.PasswordHasher;
import br.com.controlei.domain.contracts.repositories.UserRepositoryPort;
import br.com.controlei.domain.models.dtos.user.CreateUserRequest;
import br.com.controlei.domain.models.dtos.user.CurrentUserResponse;
import br.com.controlei.domain.models.dtos.user.UpdateUserRequest;
import br.com.controlei.domain.models.dtos.user.UserResponse;
import br.com.controlei.domain.models.entities.User;
import br.com.controlei.domain.models.enums.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepositoryPort userRepository;
    private final UserMapper userMapper;
    private final AuthorizationService authorizationService;
    private final PasswordHasher passwordHasher;

    public UserService(UserRepositoryPort userRepository,
                       UserMapper userMapper,
                       AuthorizationService authorizationService,
                       PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.authorizationService = authorizationService;
        this.passwordHasher = passwordHasher;
    }

    public CurrentUserResponse getCurrentUser() {
        UUID userId = authorizationService.currentUserId();
        User user = findActiveById(userId);
        return userMapper.toCurrentUserResponse(user);
    }

    public List<UserResponse> listUsers() {
        UUID familyId = authorizationService.currentFamilyId();
        return userRepository.findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtAsc(familyId)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        UUID familyId = authorizationService.currentFamilyId();

        if (!authorizationService.isResponsible()) {
            throw new ForbiddenException("Apenas o responsavel pode criar usuarios");
        }

        if (request.role() == Role.RESPONSIBLE) {
            throw new BusinessException("Nao e permitido criar outro responsavel pela familia");
        }

        if (userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new BusinessException("Email ja cadastrado");
        }

        User user = new User(
                UUID.randomUUID(),
                familyId,
                request.name(),
                request.email(),
                passwordHasher.hash(request.password()),
                request.role(),
                true,
                LocalDateTime.now(),
                null,
                null,
                null
        );

        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = findActiveById(id);
        authorizationService.requireCanWrite(user.getFamilyId(), user.getId());

        if (!user.getEmail().equalsIgnoreCase(request.email())
                && userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new BusinessException("Email ja cadastrado");
        }

        if (authorizationService.isResponsible() && user.getRole() == Role.RESPONSIBLE
                && !authorizationService.currentUserId().equals(id)) {
            throw new ForbiddenException("Nao e possivel editar outro responsavel");
        }

        user.setName(request.name());
        user.setEmail(request.email());

        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordHasher.hash(request.password()));
        }

        if (request.active() != null) {
            if (!authorizationService.isResponsible()) {
                throw new ForbiddenException("Apenas o responsavel pode alterar o status");
            }
            user.setActive(request.active());
        }

        User updated = userRepository.save(user);
        return userMapper.toResponse(updated);
    }

    private User findActiveById(UUID id) {
        User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado"));

        if (!user.isActive()) {
            throw new NotFoundException("Usuario nao encontrado");
        }

        return user;
    }
}
