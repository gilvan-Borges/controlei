package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.dtos.common.PageResult;
import br.com.controlei.domain.models.entities.Transaction;
import br.com.controlei.domain.models.enums.TransactionStatus;
import br.com.controlei.domain.models.enums.TransactionType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepositoryPort {

    Optional<Transaction> findByIdAndDeletedAtIsNull(UUID id);

    List<Transaction> findAllByFamilyIdAndPeriod(UUID familyId, LocalDate startDate, LocalDate endDate);

    /**
     * Total de despesas (nao canceladas) de uma categoria no periodo, calculado pelo banco.
     *
     * @param userId restringe a um membro; nulo soma a familia toda
     */
    java.math.BigDecimal sumExpenses(UUID familyId, UUID categoryId, UUID userId, LocalDate startDate, LocalDate endDate);

    PageResult<Transaction> findAllByFamilyIdAndFilters(UUID familyId, LocalDate startDate, LocalDate endDate,
                                                       UUID userId, UUID accountId, UUID categoryId,
                                                       TransactionType type, TransactionStatus status,
                                                       int page, int size);

    Transaction save(Transaction transaction);
}
