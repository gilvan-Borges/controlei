package br.com.controlei.infrastructure.repositories;

import br.com.controlei.infrastructure.persistence.entities.FamilyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FamilyRepository extends JpaRepository<FamilyEntity, UUID> {

    Optional<FamilyEntity> findByIdAndDeletedAtIsNull(UUID id);
}
