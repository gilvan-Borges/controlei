package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.entities.Family;

import java.util.Optional;
import java.util.UUID;

public interface FamilyRepositoryPort {

    Optional<Family> findByIdAndDeletedAtIsNull(UUID id);

    Family save(Family family);
}
