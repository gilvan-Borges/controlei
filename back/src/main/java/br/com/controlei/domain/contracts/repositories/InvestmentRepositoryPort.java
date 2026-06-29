package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.entities.Investment;
import br.com.controlei.domain.models.enums.InvestmentType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestmentRepositoryPort {

    Optional<Investment> findByIdAndDeletedAtIsNull(UUID id);

    List<Investment> findAllByFamilyIdAndFilters(UUID familyId, UUID userId, InvestmentType type,
                                                  UUID categoryId, LocalDate startDate, LocalDate endDate);

    Investment save(Investment investment);
}
