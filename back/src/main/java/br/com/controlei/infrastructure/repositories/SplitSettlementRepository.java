package br.com.controlei.infrastructure.repositories;

import br.com.controlei.infrastructure.persistence.entities.SplitSettlementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SplitSettlementRepository extends JpaRepository<SplitSettlementEntity, UUID> {

    Optional<SplitSettlementEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<SplitSettlementEntity> findAllByFamilyIdAndDeletedAtIsNullOrderBySettlementDateDescCreatedAtDesc(UUID familyId);
}
