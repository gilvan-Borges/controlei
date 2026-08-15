package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.entities.Budget;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepositoryPort {

    Budget save(Budget budget);

    Optional<Budget> findByIdAndDeletedAtIsNull(UUID id);

    Optional<Budget> findByFamilyIdAndUserIdAndCategoryIdAndYearAndMonthAndDeletedAtIsNull(
            UUID familyId, UUID userId, UUID categoryId, int year, int month);

    List<Budget> findAllByFamilyIdAndYearAndMonthAndDeletedAtIsNull(UUID familyId, int year, int month);

    List<Budget> findAllByFamilyIdAndUserIdAndYearAndMonthAndDeletedAtIsNull(UUID familyId, UUID userId, int year, int month);
}
