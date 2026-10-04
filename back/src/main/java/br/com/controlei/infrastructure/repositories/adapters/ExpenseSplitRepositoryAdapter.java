package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.ExpenseSplitRepositoryPort;
import br.com.controlei.domain.models.entities.ExpenseSplit;
import br.com.controlei.infrastructure.mappers.ExpenseSplitEntityMapper;
import br.com.controlei.infrastructure.repositories.ExpenseSplitRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ExpenseSplitRepositoryAdapter implements ExpenseSplitRepositoryPort {

    private final ExpenseSplitRepository repository;
    private final ExpenseSplitEntityMapper mapper;

    public ExpenseSplitRepositoryAdapter(ExpenseSplitRepository repository, ExpenseSplitEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public ExpenseSplit save(ExpenseSplit split) {
        return mapper.toDomain(repository.save(mapper.toEntity(split)));
    }

    @Override
    public Optional<ExpenseSplit> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public Optional<ExpenseSplit> findByTransactionIdAndDeletedAtIsNull(UUID transactionId) {
        return repository.findByTransactionIdAndDeletedAtIsNull(transactionId).map(mapper::toDomain);
    }

    @Override
    public List<ExpenseSplit> findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId) {
        return repository.findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(familyId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
