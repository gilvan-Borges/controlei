package br.com.controlei.infrastructure.repositories;

import br.com.controlei.domain.models.enums.AccountType;
import br.com.controlei.infrastructure.persistence.entities.AccountEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, UUID>, JpaSpecificationExecutor<AccountEntity> {

    Optional<AccountEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<AccountEntity> findAllByFamilyIdAndDeletedAtIsNull(UUID familyId);

    boolean existsByFamilyIdAndTypeAndNameAndDeletedAtIsNull(UUID familyId, AccountType type, String name);

    static Specification<AccountEntity> byFamilyId(UUID familyId) {
        return (root, query, cb) -> cb.equal(root.get("familyId"), familyId);
    }

    static Specification<AccountEntity> deletedAtIsNull() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    static Specification<AccountEntity> byActive(Boolean active) {
        return (root, query, cb) -> active == null ? null : cb.equal(root.get("active"), active);
    }

    static Specification<AccountEntity> byType(AccountType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }

    static Specification<AccountEntity> byShared(Boolean shared) {
        return (root, query, cb) -> shared == null ? null : cb.equal(root.get("shared"), shared);
    }

    static Specification<AccountEntity> byUserId(UUID userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("userId"), userId);
    }
}
