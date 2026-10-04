package br.com.controlei.domain.models.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ExpenseSplitShare {

    private final UUID id;
    private final UUID expenseSplitId;
    private final UUID familyId;
    private UUID userId;
    private BigDecimal shareAmount;
    private boolean settled;
    private LocalDateTime settledAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String deletedBy;

    public ExpenseSplitShare(UUID id, UUID expenseSplitId, UUID familyId, UUID userId,
                             BigDecimal shareAmount, boolean settled, LocalDateTime settledAt,
                             LocalDateTime createdAt, LocalDateTime updatedAt,
                             LocalDateTime deletedAt, String deletedBy) {
        this.id = id;
        this.expenseSplitId = expenseSplitId;
        this.familyId = familyId;
        this.userId = userId;
        this.shareAmount = shareAmount;
        this.settled = settled;
        this.settledAt = settledAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    public UUID getId() {
        return id;
    }

    public UUID getExpenseSplitId() {
        return expenseSplitId;
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

    public BigDecimal getShareAmount() {
        return shareAmount;
    }

    public void setShareAmount(BigDecimal shareAmount) {
        this.shareAmount = shareAmount;
    }

    public boolean isSettled() {
        return settled;
    }

    public void setSettled(boolean settled) {
        this.settled = settled;
    }

    public LocalDateTime getSettledAt() {
        return settledAt;
    }

    public void setSettledAt(LocalDateTime settledAt) {
        this.settledAt = settledAt;
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
