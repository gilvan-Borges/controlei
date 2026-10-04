package br.com.controlei.domain.models.entities;

import br.com.controlei.domain.models.enums.InstallmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Installment {

    private final UUID id;
    private final UUID familyId;
    private UUID userId;
    private final UUID debtId;
    private final int installmentNumber;
    private BigDecimal amount;
    private LocalDate dueDate;
    private LocalDateTime paidAt;
    private InstallmentStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String deletedBy;

    public Installment(UUID id, UUID familyId, UUID userId, UUID debtId, int installmentNumber,
                       BigDecimal amount, LocalDate dueDate, LocalDateTime paidAt,
                       InstallmentStatus status,
                       LocalDateTime createdAt, LocalDateTime updatedAt,
                       LocalDateTime deletedAt, String deletedBy) {
        this.id = id;
        this.familyId = familyId;
        this.userId = userId;
        this.debtId = debtId;
        this.installmentNumber = installmentNumber;
        this.amount = amount;
        this.dueDate = dueDate;
        this.paidAt = paidAt;
        this.status = status;
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

    public UUID getDebtId() {
        return debtId;
    }

    public int getInstallmentNumber() {
        return installmentNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public InstallmentStatus getStatus() {
        return status;
    }

    public void setStatus(InstallmentStatus status) {
        this.status = status;
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
