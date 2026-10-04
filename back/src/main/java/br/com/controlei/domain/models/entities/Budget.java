package br.com.controlei.domain.models.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Budget {

    private final UUID id;
    private final UUID familyId;
    private UUID userId;
    private UUID categoryId;
    private int year;
    private int month;
    private BigDecimal plannedAmount;
    private int alertThresholdPercent;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String deletedBy;

    public Budget(UUID id, UUID familyId, UUID userId, UUID categoryId,
                  int year, int month, BigDecimal plannedAmount, int alertThresholdPercent,
                  LocalDateTime createdAt, LocalDateTime updatedAt,
                  LocalDateTime deletedAt, String deletedBy) {
        this.id = id;
        this.familyId = familyId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.year = year;
        this.month = month;
        this.plannedAmount = plannedAmount;
        this.alertThresholdPercent = alertThresholdPercent;
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

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public BigDecimal getPlannedAmount() {
        return plannedAmount;
    }

    public void setPlannedAmount(BigDecimal plannedAmount) {
        this.plannedAmount = plannedAmount;
    }

    public int getAlertThresholdPercent() {
        return alertThresholdPercent;
    }

    public void setAlertThresholdPercent(int alertThresholdPercent) {
        this.alertThresholdPercent = alertThresholdPercent;
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
