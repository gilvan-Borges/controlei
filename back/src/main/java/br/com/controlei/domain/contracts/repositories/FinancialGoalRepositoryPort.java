package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.entities.FinancialGoal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancialGoalRepositoryPort {

    FinancialGoal save(FinancialGoal goal);

    Optional<FinancialGoal> findByIdAndDeletedAtIsNull(UUID id);

    List<FinancialGoal> findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId);

    List<FinancialGoal> findAllByFamilyIdAndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId, UUID userId);
}
