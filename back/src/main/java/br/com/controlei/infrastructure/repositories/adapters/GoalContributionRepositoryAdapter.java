package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.GoalContributionRepositoryPort;
import br.com.controlei.domain.models.entities.GoalContribution;
import br.com.controlei.infrastructure.mappers.GoalContributionEntityMapper;
import br.com.controlei.infrastructure.repositories.GoalContributionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class GoalContributionRepositoryAdapter implements GoalContributionRepositoryPort {

    private final GoalContributionRepository repository;
    private final GoalContributionEntityMapper mapper;

    public GoalContributionRepositoryAdapter(GoalContributionRepository repository,
                                            GoalContributionEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public GoalContribution save(GoalContribution contribution) {
        return mapper.toDomain(repository.save(mapper.toEntity(contribution)));
    }

    @Override
    public Optional<GoalContribution> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public List<GoalContribution> findAllByGoalIdAndDeletedAtIsNullOrderByContributionDateDescCreatedAtDesc(UUID goalId) {
        return repository.findAllByGoalIdAndDeletedAtIsNullOrderByContributionDateDescCreatedAtDesc(goalId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
