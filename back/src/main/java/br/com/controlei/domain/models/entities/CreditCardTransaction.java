package br.com.controlei.domain.models.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class CreditCardTransaction {

    private final UUID id;
    private final UUID creditCardId;
    private UUID invoiceId;
    private final UUID familyId;
    private final UUID userId;
    private UUID categoryId;
    private String description;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private int installmentNumber;
    private int totalInstallments;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String deletedBy;

    public CreditCardTransaction(UUID id, UUID creditCardId, UUID invoiceId, UUID familyId,
                                 UUID userId, UUID categoryId, String description,
                                 BigDecimal amount, LocalDate transactionDate,
                                 int installmentNumber, int totalInstallments,
                                 LocalDateTime createdAt, LocalDateTime updatedAt,
                                 LocalDateTime deletedAt, String deletedBy) {
        this.id = id;
        this.creditCardId = creditCardId;
        this.invoiceId = invoiceId;
        this.familyId = familyId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.description = description;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.installmentNumber = installmentNumber;
        this.totalInstallments = totalInstallments;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCreditCardId() {
        return creditCardId;
    }

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(UUID invoiceId) {
        this.invoiceId = invoiceId;
    }

    public UUID getFamilyId() {
        return familyId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public int getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(int installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public int getTotalInstallments() {
        return totalInstallments;
    }

    public void setTotalInstallments(int totalInstallments) {
        this.totalInstallments = totalInstallments;
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
