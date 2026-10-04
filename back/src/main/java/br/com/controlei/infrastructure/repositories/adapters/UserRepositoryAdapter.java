package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.UserRepositoryPort;
import br.com.controlei.domain.models.entities.User;
import br.com.controlei.infrastructure.mappers.UserEntityMapper;
import br.com.controlei.infrastructure.repositories.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserRepository repository;
    private final UserEntityMapper mapper;

    public UserRepositoryAdapter(UserRepository repository, UserEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<User> findByEmailAndDeletedAtIsNull(String email) {
        return repository.findByEmailAndDeletedAtIsNull(email).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public List<User> findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtAsc(UUID familyId) {
        return repository.findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtAsc(familyId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByEmailAndDeletedAtIsNull(String email) {
        return repository.existsByEmailAndDeletedAtIsNull(email);
    }

    @Override
    public User save(User user) {
        return mapper.toDomain(repository.save(mapper.toEntity(user)));
    }
}
