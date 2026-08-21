package br.com.controlei.domain.models.entities;

import br.com.controlei.domain.models.enums.PlanType;
import br.com.controlei.domain.models.enums.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Subscription {

    private final UUID id;
    private final UUID familyId;
    private final UUID ownerId;
    private PlanType planType;
    private SubscriptionStatus status;
    private int baseMembersAllowed;
    private int extraMembersPaid;
    private BigDecimal priceMonthly;
    private LocalDateTime trialEndsAt;
    private LocalDateTime currentPeriodStartsAt;
    private LocalDateTime currentPeriodEndsAt;
    private int graceDaysRemaining;
    private boolean autoRenew;
    private String paymentMethod;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String deletedBy;

    public Subscription(UUID id, UUID familyId, UUID ownerId, PlanType planType,
                        SubscriptionStatus status, int baseMembersAllowed, int extraMembersPaid,
                        BigDecimal priceMonthly, LocalDateTime trialEndsAt,
                        LocalDateTime currentPeriodStartsAt, LocalDateTime currentPeriodEndsAt,
                        int graceDaysRemaining, boolean autoRenew, String paymentMethod,
                        LocalDateTime createdAt, LocalDateTime updatedAt,
                        LocalDateTime deletedAt, String deletedBy) {
        this.id = id;
        this.familyId = familyId;
        this.ownerId = ownerId;
        this.planType = planType;
        this.status = status;
        this.baseMembersAllowed = baseMembersAllowed;
        this.extraMembersPaid = extraMembersPaid;
        this.priceMonthly = priceMonthly;
        this.trialEndsAt = trialEndsAt;
        this.currentPeriodStartsAt = currentPeriodStartsAt;
        this.currentPeriodEndsAt = currentPeriodEndsAt;
        this.graceDaysRemaining = graceDaysRemaining;
        this.autoRenew = autoRenew;
        this.paymentMethod = paymentMethod;
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

    public UUID getOwnerId() {
        return ownerId;
    }

    public PlanType getPlanType() {
        return planType;
    }

    public void setPlanType(PlanType planType) {
        this.planType = planType;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public int getBaseMembersAllowed() {
        return baseMembersAllowed;
    }

    public void setBaseMembersAllowed(int baseMembersAllowed) {
        this.baseMembersAllowed = baseMembersAllowed;
    }

    public int getExtraMembersPaid() {
        return extraMembersPaid;
    }

    public void setExtraMembersPaid(int extraMembersPaid) {
        this.extraMembersPaid = extraMembersPaid;
    }

    public int getTotalMembersAllowed() {
        return baseMembersAllowed + extraMembersPaid;
    }

    public BigDecimal getPriceMonthly() {
        return priceMonthly;
    }

    public void setPriceMonthly(BigDecimal priceMonthly) {
        this.priceMonthly = priceMonthly;
    }

    public LocalDateTime getTrialEndsAt() {
        return trialEndsAt;
    }

    public void setTrialEndsAt(LocalDateTime trialEndsAt) {
        this.trialEndsAt = trialEndsAt;
    }

    public LocalDateTime getCurrentPeriodStartsAt() {
        return currentPeriodStartsAt;
    }

    public void setCurrentPeriodStartsAt(LocalDateTime currentPeriodStartsAt) {
        this.currentPeriodStartsAt = currentPeriodStartsAt;
    }

    public LocalDateTime getCurrentPeriodEndsAt() {
        return currentPeriodEndsAt;
    }

    public void setCurrentPeriodEndsAt(LocalDateTime currentPeriodEndsAt) {
        this.currentPeriodEndsAt = currentPeriodEndsAt;
    }

    public int getGraceDaysRemaining() {
        return graceDaysRemaining;
    }

    public void setGraceDaysRemaining(int graceDaysRemaining) {
        this.graceDaysRemaining = graceDaysRemaining;
    }

    public boolean isAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
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
}
