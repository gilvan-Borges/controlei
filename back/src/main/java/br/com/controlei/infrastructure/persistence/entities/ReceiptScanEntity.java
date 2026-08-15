package br.com.controlei.infrastructure.persistence.entities;

import br.com.controlei.domain.models.enums.ScanStatus;
import br.com.controlei.infrastructure.configurations.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "receipt_scans")
public class ReceiptScanEntity extends AuditableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "attachment_id", nullable = false)
    private UUID attachmentId;

    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;

    @Column(name = "extracted_amount", precision = 19, scale = 4)
    private BigDecimal extractedAmount;

    @Column(name = "extracted_date")
    private LocalDate extractedDate;

    @Column(name = "extracted_merchant")
    private String extractedMerchant;

    @Column(name = "suggested_category_id")
    private UUID suggestedCategoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ScanStatus status;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(UUID attachmentId) {
        this.attachmentId = attachmentId;
    }

    public UUID getFamilyId() {
        return familyId;
    }

    public void setFamilyId(UUID familyId) {
        this.familyId = familyId;
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
}
