package br.com.controlei.domain.models.entities;

import br.com.controlei.domain.models.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Invoice {

    private final UUID id;
    private final UUID creditCardId;
    private final UUID familyId;
    private final UUID userId;
    private final LocalDate referenceMonth;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private InvoiceStatus status;
    private LocalDate dueDate;
    private LocalDateTime paidAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String deletedBy;

    public Invoice(UUID id, UUID creditCardId, UUID familyId, UUID userId, LocalDate referenceMonth,
                   BigDecimal totalAmount, BigDecimal paidAmount, InvoiceStatus status,
                   LocalDate dueDate, LocalDateTime paidAt, LocalDateTime createdAt,
                   LocalDateTime updatedAt, LocalDateTime deletedAt, String deletedBy) {
        this.id = id;
        this.creditCardId = creditCardId;
        this.familyId = familyId;
        this.userId = userId;
        this.referenceMonth = referenceMonth;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.status = status;
        this.dueDate = dueDate;
        this.paidAt = paidAt;
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

    public UUID getFamilyId() {
        return familyId;
    }

    public UUID getUserId() {
        return userId;
    }

    public LocalDate getReferenceMonth() {
        return referenceMonth;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
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
