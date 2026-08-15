package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.entities.SplitSettlement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SplitSettlementRepositoryPort {

    SplitSettlement save(SplitSettlement settlement);

    Optional<SplitSettlement> findByIdAndDeletedAtIsNull(UUID id);

    List<SplitSettlement> findAllByFamilyIdAndDeletedAtIsNullOrderBySettlementDateDescCreatedAtDesc(UUID familyId);
}
