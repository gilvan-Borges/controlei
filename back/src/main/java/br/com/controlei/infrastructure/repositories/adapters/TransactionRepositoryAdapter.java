package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.TransactionRepositoryPort;
import br.com.controlei.domain.models.dtos.common.PageResult;
import br.com.controlei.domain.models.entities.Transaction;
import br.com.controlei.domain.models.enums.TransactionStatus;
import br.com.controlei.domain.models.enums.TransactionType;
import br.com.controlei.infrastructure.mappers.TransactionEntityMapper;
import br.com.controlei.infrastructure.persistence.entities.TransactionEntity;
import br.com.controlei.infrastructure.repositories.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Component
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {

    private final TransactionRepository repository;
    private final TransactionEntityMapper mapper;

    public TransactionRepositoryAdapter(TransactionRepository repository, TransactionEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Transaction> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public PageResult<Transaction> findAllByFamilyIdAndFilters(UUID familyId, LocalDate startDate, LocalDate endDate,
                                                               UUID userId, UUID accountId, UUID categoryId,
                                                               TransactionType type, TransactionStatus status,
                                                               int page, int size) {
        Specification<TransactionEntity> spec = Specification.where(TransactionRepository.byFamilyId(familyId))
                .and(TransactionRepository.deletedAtIsNull())
                .and(TransactionRepository.transactionDateBetween(startDate, endDate))
                .and(TransactionRepository.byUserId(userId))
                .and(TransactionRepository.byAccountId(accountId))
                .and(TransactionRepository.byCategoryId(categoryId))
                .and(TransactionRepository.byType(type))
                .and(TransactionRepository.byStatus(status));

        Page<TransactionEntity> pageResult = repository.findAll(spec, PageRequest.of(page, size));
        return new PageResult<>(
                pageResult.getContent().stream().map(mapper::toDomain).toList(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = mapper.toEntity(transaction);
        TransactionEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }
}
