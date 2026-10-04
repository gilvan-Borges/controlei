package br.com.controlei.infrastructure.repositories;

import br.com.controlei.domain.models.enums.TransactionStatus;
import br.com.controlei.domain.models.enums.TransactionType;
import br.com.controlei.infrastructure.persistence.entities.TransactionEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID>, JpaSpecificationExecutor<TransactionEntity> {

    Optional<TransactionEntity> findByIdAndDeletedAtIsNull(UUID id);

    /** Despesas da categoria no periodo, somadas pelo banco. Canceladas nao contam como gasto. */
    @Query("""
            SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t
            WHERE t.familyId = :familyId AND t.categoryId = :categoryId
              AND t.type = br.com.controlei.domain.models.enums.TransactionType.EXPENSE
              AND t.status <> br.com.controlei.domain.models.enums.TransactionStatus.CANCELED
              AND t.deletedAt IS NULL AND t.transactionDate BETWEEN :start AND :end""")
    java.math.BigDecimal sumExpensesByCategory(@Param("familyId") UUID familyId, @Param("categoryId") UUID categoryId,
                                               @Param("start") LocalDate start, @Param("end") LocalDate end);

    /** Igual, restrita a um membro (orcamento individual). */
    @Query("""
            SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t
            WHERE t.familyId = :familyId AND t.categoryId = :categoryId AND t.userId = :userId
              AND t.type = br.com.controlei.domain.models.enums.TransactionType.EXPENSE
              AND t.status <> br.com.controlei.domain.models.enums.TransactionStatus.CANCELED
              AND t.deletedAt IS NULL AND t.transactionDate BETWEEN :start AND :end""")
    java.math.BigDecimal sumExpensesByCategoryAndUser(@Param("familyId") UUID familyId, @Param("categoryId") UUID categoryId,
                                                      @Param("userId") UUID userId,
                                                      @Param("start") LocalDate start, @Param("end") LocalDate end);

    static Specification<TransactionEntity> byFamilyId(UUID familyId) {
        return (root, query, cb) -> cb.equal(root.get("familyId"), familyId);
    }

    static Specification<TransactionEntity> deletedAtIsNull() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    static Specification<TransactionEntity> transactionDateBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            if (startDate != null && endDate != null) {
                return cb.between(root.get("transactionDate"), startDate, endDate);
            }
            if (startDate != null) {
                return cb.greaterThanOrEqualTo(root.get("transactionDate"), startDate);
            }
            if (endDate != null) {
                return cb.lessThanOrEqualTo(root.get("transactionDate"), endDate);
            }
            return null;
        };
    }

    static Specification<TransactionEntity> byUserId(UUID userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("userId"), userId);
    }

    static Specification<TransactionEntity> byAccountId(UUID accountId) {
        return (root, query, cb) -> accountId == null ? null : cb.equal(root.get("accountId"), accountId);
    }

    static Specification<TransactionEntity> byCategoryId(UUID categoryId) {
        return (root, query, cb) -> categoryId == null ? null : cb.equal(root.get("categoryId"), categoryId);
    }

    static Specification<TransactionEntity> byType(TransactionType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }

    static Specification<TransactionEntity> byStatus(TransactionStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }
}
