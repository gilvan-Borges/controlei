package br.com.controlei.infrastructure.repositories;

import br.com.controlei.domain.models.enums.CategoryType;
import br.com.controlei.infrastructure.persistence.entities.CategoryEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID>, JpaSpecificationExecutor<CategoryEntity> {

    Optional<CategoryEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<CategoryEntity> findAllByFamilyIdAndDeletedAtIsNull(UUID familyId);

    boolean existsByFamilyIdAndTypeAndNameAndDeletedAtIsNull(UUID familyId, CategoryType type, String name);

    static Specification<CategoryEntity> byFamilyId(UUID familyId) {
        return (root, query, cb) -> cb.equal(root.get("familyId"), familyId);
    }

    static Specification<CategoryEntity> deletedAtIsNull() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    static Specification<CategoryEntity> byActive(Boolean active) {
        return (root, query, cb) -> active == null ? null : cb.equal(root.get("active"), active);
    }

    static Specification<CategoryEntity> byType(CategoryType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }
}
