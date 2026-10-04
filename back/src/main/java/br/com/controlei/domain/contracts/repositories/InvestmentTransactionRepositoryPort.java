package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.entities.InvestmentTransaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestmentTransactionRepositoryPort {

    InvestmentTransaction save(InvestmentTransaction transaction);

    Optional<InvestmentTransaction> findByIdAndDeletedAtIsNull(UUID id);

    List<InvestmentTransaction> findAllByInvestmentIdAndDeletedAtIsNullOrderByTransactionDateDescCreatedAtDesc(UUID investmentId);

    List<InvestmentTransaction> findAllByFamilyIdAndDeletedAtIsNullOrderByTransactionDateDescCreatedAtDesc(UUID familyId);
}
