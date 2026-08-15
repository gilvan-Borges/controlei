package br.com.controlei.infrastructure.repositories;

import br.com.controlei.infrastructure.persistence.entities.RecurringTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransactionEntity, UUID> {

    Optional<RecurringTransactionEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<RecurringTransactionEntity> findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId);

    List<RecurringTransactionEntity> findAllByFamilyIdAndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId, UUID userId);

    @Query("SELECT r FROM RecurringTransactionEntity r WHERE r.active = true AND r.deletedAt IS NULL AND r.nextExecutionDate <= :date ORDER BY r.nextExecutionDate ASC")
    List<RecurringTransactionEntity> findAllActiveDueOnOrBefore(LocalDate date);
}
