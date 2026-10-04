package br.com.controlei.infrastructure.repositories;

import br.com.controlei.infrastructure.persistence.entities.ExpenseSplitShareEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpenseSplitShareRepository extends JpaRepository<ExpenseSplitShareEntity, UUID> {

    Optional<ExpenseSplitShareEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<ExpenseSplitShareEntity> findAllByExpenseSplitIdAndDeletedAtIsNull(UUID expenseSplitId);

    List<ExpenseSplitShareEntity> findAllByFamilyIdAndDeletedAtIsNull(UUID familyId);

    List<ExpenseSplitShareEntity> findAllByFamilyIdAndUserIdAndDeletedAtIsNull(UUID familyId, UUID userId);
}
