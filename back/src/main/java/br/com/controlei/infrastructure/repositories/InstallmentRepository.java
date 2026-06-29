package br.com.controlei.infrastructure.repositories;

import br.com.controlei.domain.models.enums.InstallmentStatus;
import br.com.controlei.infrastructure.persistence.entities.InstallmentEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstallmentRepository extends JpaRepository<InstallmentEntity, UUID>, JpaSpecificationExecutor<InstallmentEntity> {

    Optional<InstallmentEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<InstallmentEntity> findAllByDebtIdAndDeletedAtIsNullOrderByInstallmentNumberAsc(UUID debtId);

    static Specification<InstallmentEntity> byFamilyId(UUID familyId) {
        return (root, query, cb) -> cb.equal(root.get("familyId"), familyId);
    }

    static Specification<InstallmentEntity> deletedAtIsNull() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    static Specification<InstallmentEntity> byUserId(UUID userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("userId"), userId);
    }

    static Specification<InstallmentEntity> byDebtId(UUID debtId) {
        return (root, query, cb) -> debtId == null ? null : cb.equal(root.get("debtId"), debtId);
    }

    static Specification<InstallmentEntity> byStatus(InstallmentStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    static Specification<InstallmentEntity> dueDateBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            if (startDate != null && endDate != null) {
                return cb.between(root.get("dueDate"), startDate, endDate);
            }
            if (startDate != null) {
                return cb.greaterThanOrEqualTo(root.get("dueDate"), startDate);
            }
            if (endDate != null) {
                return cb.lessThanOrEqualTo(root.get("dueDate"), endDate);
            }
            return null;
        };
    }
}
