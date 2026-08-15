package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.entities.GoalContribution;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalContributionRepositoryPort {

    GoalContribution save(GoalContribution contribution);

    Optional<GoalContribution> findByIdAndDeletedAtIsNull(UUID id);

    List<GoalContribution> findAllByGoalIdAndDeletedAtIsNullOrderByContributionDateDescCreatedAtDesc(UUID goalId);
}
