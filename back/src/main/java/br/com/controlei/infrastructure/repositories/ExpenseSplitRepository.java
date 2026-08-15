package br.com.controlei.infrastructure.repositories;

import br.com.controlei.infrastructure.persistence.entities.ExpenseSplitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplitEntity, UUID> {

    Optional<ExpenseSplitEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<ExpenseSplitEntity> findByTransactionIdAndDeletedAtIsNull(UUID transactionId);

    List<ExpenseSplitEntity> findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId);
}
