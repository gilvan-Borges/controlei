package br.com.controlei.domain.models.entities;

import br.com.controlei.domain.models.enums.ScanStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ReceiptScan {

    private final UUID id;
    private final UUID attachmentId;
    private final UUID familyId;
    private UUID userId;
    private String rawText;
    private BigDecimal extractedAmount;
    private LocalDate extractedDate;
    private String extractedMerchant;
    private UUID suggestedCategoryId;
    private ScanStatus status;
    private BigDecimal confidenceScore;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String deletedBy;

    public ReceiptScan(UUID id, UUID attachmentId, UUID familyId, UUID userId, String rawText,
                       BigDecimal extractedAmount, LocalDate extractedDate, String extractedMerchant,
                       UUID suggestedCategoryId, ScanStatus status, BigDecimal confidenceScore,
                       LocalDateTime createdAt, LocalDateTime updatedAt,
                       LocalDateTime deletedAt, String deletedBy) {
        this.id = id;
        this.attachmentId = attachmentId;
        this.familyId = familyId;
        this.userId = userId;
        this.rawText = rawText;
        this.extractedAmount = extractedAmount;
        this.extractedDate = extractedDate;
        this.extractedMerchant = extractedMerchant;
        this.suggestedCategoryId = suggestedCategoryId;
        this.status = status;
        this.confidenceScore = confidenceScore;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAttachmentId() {
        return attachmentId;
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

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public BigDecimal getExtractedAmount() {
        return extractedAmount;
    }

    public void setExtractedAmount(BigDecimal extractedAmount) {
        this.extractedAmount = extractedAmount;
    }

    public LocalDate getExtractedDate() {
        return extractedDate;
    }

    public void setExtractedDate(LocalDate extractedDate) {
        this.extractedDate = extractedDate;
    }

    public String getExtractedMerchant() {
        return extractedMerchant;
    }

    public void setExtractedMerchant(String extractedMerchant) {
        this.extractedMerchant = extractedMerchant;
    }

    public UUID getSuggestedCategoryId() {
        return suggestedCategoryId;
    }

    public void setSuggestedCategoryId(UUID suggestedCategoryId) {
        this.suggestedCategoryId = suggestedCategoryId;
    }

    public ScanStatus getStatus() {
        return status;
    }

    public void setStatus(ScanStatus status) {
        this.status = status;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
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
