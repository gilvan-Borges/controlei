package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.dtos.common.PageResult;
import br.com.controlei.domain.models.entities.Transaction;
import br.com.controlei.domain.models.enums.TransactionStatus;
import br.com.controlei.domain.models.enums.TransactionType;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepositoryPort {

    Optional<Transaction> findByIdAndDeletedAtIsNull(UUID id);

    PageResult<Transaction> findAllByFamilyIdAndFilters(UUID familyId, LocalDate startDate, LocalDate endDate,
                                                       UUID userId, UUID accountId, UUID categoryId,
                                                       TransactionType type, TransactionStatus status,
                                                       int page, int size);

    Transaction save(Transaction transaction);
}
