package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.entities.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByIdAndDeletedAtIsNull(UUID id);

    List<User> findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtAsc(UUID familyId);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    User save(User user);
}
