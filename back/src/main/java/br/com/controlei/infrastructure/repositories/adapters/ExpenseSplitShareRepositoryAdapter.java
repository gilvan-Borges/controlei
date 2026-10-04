package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.ExpenseSplitShareRepositoryPort;
import br.com.controlei.domain.models.entities.ExpenseSplitShare;
import br.com.controlei.infrastructure.mappers.ExpenseSplitShareEntityMapper;
import br.com.controlei.infrastructure.repositories.ExpenseSplitShareRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ExpenseSplitShareRepositoryAdapter implements ExpenseSplitShareRepositoryPort {

    private final ExpenseSplitShareRepository repository;
    private final ExpenseSplitShareEntityMapper mapper;

    public ExpenseSplitShareRepositoryAdapter(ExpenseSplitShareRepository repository,
                                              ExpenseSplitShareEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public ExpenseSplitShare save(ExpenseSplitShare share) {
        return mapper.toDomain(repository.save(mapper.toEntity(share)));
    }

    @Override
    public Optional<ExpenseSplitShare> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public List<ExpenseSplitShare> findAllByExpenseSplitIdAndDeletedAtIsNull(UUID expenseSplitId) {
        return repository.findAllByExpenseSplitIdAndDeletedAtIsNull(expenseSplitId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<ExpenseSplitShare> findAllByFamilyIdAndDeletedAtIsNull(UUID familyId) {
        return repository.findAllByFamilyIdAndDeletedAtIsNull(familyId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<ExpenseSplitShare> findAllByFamilyIdAndUserIdAndDeletedAtIsNull(UUID familyId, UUID userId) {
        return repository.findAllByFamilyIdAndUserIdAndDeletedAtIsNull(familyId, userId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
