package br.com.controlei.infrastructure.repositories;

import br.com.controlei.infrastructure.persistence.entities.GoalContributionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GoalContributionRepository extends JpaRepository<GoalContributionEntity, UUID> {

    Optional<GoalContributionEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<GoalContributionEntity> findAllByGoalIdAndDeletedAtIsNullOrderByContributionDateDescCreatedAtDesc(UUID goalId);
}
