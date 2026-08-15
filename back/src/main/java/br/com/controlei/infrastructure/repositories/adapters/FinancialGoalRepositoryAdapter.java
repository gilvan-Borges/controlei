package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.FinancialGoalRepositoryPort;
import br.com.controlei.domain.models.entities.FinancialGoal;
import br.com.controlei.infrastructure.mappers.FinancialGoalEntityMapper;
import br.com.controlei.infrastructure.repositories.FinancialGoalRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class FinancialGoalRepositoryAdapter implements FinancialGoalRepositoryPort {

    private final FinancialGoalRepository repository;
    private final FinancialGoalEntityMapper mapper;

    public FinancialGoalRepositoryAdapter(FinancialGoalRepository repository, FinancialGoalEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public FinancialGoal save(FinancialGoal goal) {
        return mapper.toDomain(repository.save(mapper.toEntity(goal)));
    }

    @Override
    public Optional<FinancialGoal> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public List<FinancialGoal> findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId) {
        return repository.findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(familyId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<FinancialGoal> findAllByFamilyIdAndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId, UUID userId) {
        return repository.findAllByFamilyIdAndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(familyId, userId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
