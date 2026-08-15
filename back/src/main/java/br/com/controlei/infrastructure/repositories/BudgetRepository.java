package br.com.controlei.infrastructure.repositories;

import br.com.controlei.infrastructure.persistence.entities.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<BudgetEntity, UUID> {

    Optional<BudgetEntity> findByIdAndDeletedAtIsNull(UUID id);

    @Query("SELECT b FROM BudgetEntity b WHERE b.familyId = :familyId " +
           "AND (:userId IS NULL AND b.userId IS NULL OR b.userId = :userId) " +
           "AND b.categoryId = :categoryId AND b.year = :year AND b.month = :month AND b.deletedAt IS NULL")
    Optional<BudgetEntity> findByFamilyIdAndUserIdAndCategoryIdAndYearAndMonthAndDeletedAtIsNull(
            UUID familyId, UUID userId, UUID categoryId, int year, int month);

    List<BudgetEntity> findAllByFamilyIdAndYearAndMonthAndDeletedAtIsNull(UUID familyId, int year, int month);

    List<BudgetEntity> findAllByFamilyIdAndUserIdAndYearAndMonthAndDeletedAtIsNull(UUID familyId, UUID userId, int year, int month);
}
