package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.entities.Debt;
import br.com.controlei.domain.models.enums.DebtStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DebtRepositoryPort {

    Optional<Debt> findByIdAndDeletedAtIsNull(UUID id);

    List<Debt> findAllByFamilyIdAndFilters(UUID familyId, UUID userId, DebtStatus status);

    Debt save(Debt debt);
}
