package br.com.controlei.infrastructure.repositories;

import br.com.controlei.infrastructure.persistence.entities.InvestmentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvestmentTransactionRepository extends JpaRepository<InvestmentTransactionEntity, UUID> {

    Optional<InvestmentTransactionEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<InvestmentTransactionEntity> findAllByInvestmentIdAndDeletedAtIsNullOrderByTransactionDateDescCreatedAtDesc(UUID investmentId);

    List<InvestmentTransactionEntity> findAllByFamilyIdAndDeletedAtIsNullOrderByTransactionDateDescCreatedAtDesc(UUID familyId);
}
