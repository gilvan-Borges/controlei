package br.com.controlei.domain.models.entities;

import br.com.controlei.domain.models.enums.BankConnectionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class BankConnection {

    private final UUID id;
    private final UUID familyId;
    private UUID userId;
    private String institutionId;
    private String institutionName;
    private String externalItemId;
    private BankConnectionStatus status;
    private LocalDateTime lastSyncedAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String deletedBy;

    public BankConnection(UUID id, UUID familyId, UUID userId, String institutionId,
                          String institutionName, String externalItemId, BankConnectionStatus status,
                          LocalDateTime lastSyncedAt, LocalDateTime createdAt,
                          LocalDateTime updatedAt, LocalDateTime deletedAt, String deletedBy) {
        this.id = id;
        this.familyId = familyId;
        this.userId = userId;
        this.institutionId = institutionId;
        this.institutionName = institutionName;
        this.externalItemId = externalItemId;
        this.status = status;
        this.lastSyncedAt = lastSyncedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    public UUID getId() {
        return id;
    }

    public UUID getFamilyId() {
        return familyId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(String institutionId) {
        this.institutionId = institutionId;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getExternalItemId() {
        return externalItemId;
    }

    public void setExternalItemId(String externalItemId) {
        this.externalItemId = externalItemId;
    }

    public BankConnectionStatus getStatus() {
        return status;
    }

    public void setStatus(BankConnectionStatus status) {
        this.status = status;
    }

    public LocalDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(LocalDateTime lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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
