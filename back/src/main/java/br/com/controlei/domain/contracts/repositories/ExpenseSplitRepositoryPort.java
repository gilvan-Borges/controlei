package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.entities.ExpenseSplit;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseSplitRepositoryPort {

    ExpenseSplit save(ExpenseSplit split);

    Optional<ExpenseSplit> findByIdAndDeletedAtIsNull(UUID id);

    Optional<ExpenseSplit> findByTransactionIdAndDeletedAtIsNull(UUID transactionId);

    List<ExpenseSplit> findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId);
}
