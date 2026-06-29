package br.com.controlei.infrastructure.repositories;

import br.com.controlei.domain.models.enums.DebtStatus;
import br.com.controlei.infrastructure.persistence.entities.DebtEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DebtRepository extends JpaRepository<DebtEntity, UUID>, JpaSpecificationExecutor<DebtEntity> {

    Optional<DebtEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<DebtEntity> findAllByFamilyIdAndDeletedAtIsNull(UUID familyId);

    static Specification<DebtEntity> byFamilyId(UUID familyId) {
        return (root, query, cb) -> cb.equal(root.get("familyId"), familyId);
    }

    static Specification<DebtEntity> deletedAtIsNull() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    static Specification<DebtEntity> byUserId(UUID userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("userId"), userId);
    }

    static Specification<DebtEntity> byStatus(DebtStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }
}
