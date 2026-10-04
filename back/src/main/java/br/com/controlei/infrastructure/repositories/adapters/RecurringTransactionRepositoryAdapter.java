package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.RecurringTransactionRepositoryPort;
import br.com.controlei.domain.models.entities.RecurringTransaction;
import br.com.controlei.infrastructure.mappers.RecurringTransactionEntityMapper;
import br.com.controlei.infrastructure.repositories.RecurringTransactionRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RecurringTransactionRepositoryAdapter implements RecurringTransactionRepositoryPort {

    private final RecurringTransactionRepository repository;
    private final RecurringTransactionEntityMapper mapper;

    public RecurringTransactionRepositoryAdapter(RecurringTransactionRepository repository,
                                                 RecurringTransactionEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public RecurringTransaction save(RecurringTransaction recurringTransaction) {
        return mapper.toDomain(repository.save(mapper.toEntity(recurringTransaction)));
    }

    @Override
    public Optional<RecurringTransaction> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public List<RecurringTransaction> findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId) {
        return repository.findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(familyId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<RecurringTransaction> findAllByFamilyIdAndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId, UUID userId) {
        return repository.findAllByFamilyIdAndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(familyId, userId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<RecurringTransaction> findAllActiveDueOnOrBefore(LocalDate date) {
        return repository.findAllActiveDueOnOrBefore(date)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
