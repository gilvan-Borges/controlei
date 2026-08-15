package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.entities.RecurringTransaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurringTransactionRepositoryPort {

    RecurringTransaction save(RecurringTransaction recurringTransaction);

    Optional<RecurringTransaction> findByIdAndDeletedAtIsNull(UUID id);

    List<RecurringTransaction> findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId);

    List<RecurringTransaction> findAllByFamilyIdAndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId, UUID userId);

    List<RecurringTransaction> findAllActiveDueOnOrBefore(LocalDate date);
}
