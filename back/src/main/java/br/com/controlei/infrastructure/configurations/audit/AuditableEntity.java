package br.com.controlei.infrastructure.configurations.audit;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Base class for entities requiring audit trail and soft delete.
 *
 * IMPORTANT: This class is JPA-specific and belongs to infrastructure layer.
 * Domain entities should NOT extend this class directly to avoid violating
 * the dependency rule (domain cannot depend on infrastructure/JPA).
 *
 * Recommended pattern:
 * 1. Create pure domain entities in domain.models.entities without JPA annotations
 * 2. Create JPA entities in infrastructure.persistence.entities that extend this class
 * 3. Use mappers to convert between domain entities and JPA entities
 *
 * Alternative pattern (simpler, acceptable for this project):
 * - Use JPA entities directly but keep them in infrastructure layer
 * - Domain layer uses DTOs or interfaces to interact with data
 *
 * Usage:
 * 1. Extend this class in your JPA entity (in infrastructure.persistence.entities)
 * 2. Ensure the entity table has columns: created_at, created_by, updated_at, updated_by, deleted_at, deleted_by
 * 3. For soft delete, set deletedAt and deletedBy instead of physical delete
 * 4. Queries should filter by "deleted_at IS NULL" to get active records
 *
 * Example:
 * @Entity
 * @Table(name = "families")
 * public class FamilyEntity extends AuditableEntity {
 *     // your fields
 * }
 *
 * Repository query example:
 * @Query("SELECT f FROM FamilyEntity f WHERE f.deletedAt IS NULL")
 * List<FamilyEntity> findAllActive();
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
