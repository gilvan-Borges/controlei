package br.com.controlei.domain.models.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class BankSyncMapping {

    private final UUID id;
    private final UUID bankConnectionId;
    private final UUID familyId;
    private UUID accountId;
    private UUID creditCardId;
    private String externalAccountId;
    private LocalDate lastTransactionSyncDate;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String deletedBy;

    public BankSyncMapping(UUID id, UUID bankConnectionId, UUID familyId, UUID accountId,
                           UUID creditCardId, String externalAccountId, LocalDate lastTransactionSyncDate,
                           LocalDateTime createdAt, LocalDateTime updatedAt,
                           LocalDateTime deletedAt, String deletedBy) {
        this.id = id;
        this.bankConnectionId = bankConnectionId;
        this.familyId = familyId;
        this.accountId = accountId;
        this.creditCardId = creditCardId;
        this.externalAccountId = externalAccountId;
        this.lastTransactionSyncDate = lastTransactionSyncDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBankConnectionId() {
        return bankConnectionId;
    }

    public UUID getFamilyId() {
        return familyId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getCreditCardId() {
        return creditCardId;
    }

    public void setCreditCardId(UUID creditCardId) {
        this.creditCardId = creditCardId;
    }

    public String getExternalAccountId() {
        return externalAccountId;
    }

    public void setExternalAccountId(String externalAccountId) {
        this.externalAccountId = externalAccountId;
    }

    public LocalDate getLastTransactionSyncDate() {
        return lastTransactionSyncDate;
    }

    public void setLastTransactionSyncDate(LocalDate lastTransactionSyncDate) {
        this.lastTransactionSyncDate = lastTransactionSyncDate;
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
