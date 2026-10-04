package br.com.controlei.infrastructure.repositories;

import br.com.controlei.domain.models.enums.InvestmentType;
import br.com.controlei.infrastructure.persistence.entities.InvestmentEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvestmentRepository extends JpaRepository<InvestmentEntity, UUID>, JpaSpecificationExecutor<InvestmentEntity> {

    Optional<InvestmentEntity> findByIdAndDeletedAtIsNull(UUID id);

    @Query("SELECT COALESCE(SUM(i.currentAmount), 0) FROM InvestmentEntity i " +
           "WHERE i.familyId = :familyId AND i.deletedAt IS NULL " +
           "AND (:userId IS NULL OR i.userId = :userId)")
    BigDecimal sumCurrentAmountByFilters(@Param("familyId") UUID familyId,
                                          @Param("userId") UUID userId);

    static Specification<InvestmentEntity> byFamilyId(UUID familyId) {
        return (root, query, cb) -> cb.equal(root.get("familyId"), familyId);
    }

    static Specification<InvestmentEntity> deletedAtIsNull() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    static Specification<InvestmentEntity> byUserId(UUID userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("userId"), userId);
    }

    static Specification<InvestmentEntity> byType(InvestmentType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }

    static Specification<InvestmentEntity> byCategoryId(UUID categoryId) {
        return (root, query, cb) -> categoryId == null ? null : cb.equal(root.get("categoryId"), categoryId);
    }

    static Specification<InvestmentEntity> referenceDateBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            if (startDate != null && endDate != null) {
                return cb.between(root.get("referenceDate"), startDate, endDate);
            }
            if (startDate != null) {
                return cb.greaterThanOrEqualTo(root.get("referenceDate"), startDate);
            }
            if (endDate != null) {
                return cb.lessThanOrEqualTo(root.get("referenceDate"), endDate);
            }
            return null;
        };
    }
}
