package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.entities.ExpenseSplitShare;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseSplitShareRepositoryPort {

    ExpenseSplitShare save(ExpenseSplitShare share);

    Optional<ExpenseSplitShare> findByIdAndDeletedAtIsNull(UUID id);

    List<ExpenseSplitShare> findAllByExpenseSplitIdAndDeletedAtIsNull(UUID expenseSplitId);

    List<ExpenseSplitShare> findAllByFamilyIdAndDeletedAtIsNull(UUID familyId);

    List<ExpenseSplitShare> findAllByFamilyIdAndUserIdAndDeletedAtIsNull(UUID familyId, UUID userId);
}
