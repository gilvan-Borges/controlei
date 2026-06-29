package br.com.controlei.infrastructure.repositories;

import br.com.controlei.infrastructure.persistence.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmailAndDeletedAtIsNull(String email);

    Optional<UserEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<UserEntity> findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtAsc(UUID familyId);

    boolean existsByEmailAndDeletedAtIsNull(String email);
}
