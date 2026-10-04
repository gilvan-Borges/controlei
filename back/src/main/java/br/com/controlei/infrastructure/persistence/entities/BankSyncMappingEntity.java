package br.com.controlei.infrastructure.persistence.entities;

import br.com.controlei.infrastructure.configurations.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "bank_sync_mappings")
public class BankSyncMappingEntity extends AuditableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "bank_connection_id", nullable = false)
    private UUID bankConnectionId;

    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "credit_card_id")
    private UUID creditCardId;

    @Column(name = "external_account_id", nullable = false)
    private String externalAccountId;

    @Column(name = "last_transaction_sync_date")
    private LocalDate lastTransactionSyncDate;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBankConnectionId() {
        return bankConnectionId;
    }

    public void setBankConnectionId(UUID bankConnectionId) {
        this.bankConnectionId = bankConnectionId;
    }

    public UUID getFamilyId() {
        return familyId;
    }

    public void setFamilyId(UUID familyId) {
        this.familyId = familyId;
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
}
