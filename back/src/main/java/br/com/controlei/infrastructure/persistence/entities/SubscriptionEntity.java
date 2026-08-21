package br.com.controlei.infrastructure.persistence.entities;

import br.com.controlei.domain.models.enums.PlanType;
import br.com.controlei.domain.models.enums.SubscriptionStatus;
import br.com.controlei.infrastructure.configurations.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
public class SubscriptionEntity extends AuditableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "family_id", nullable = false, unique = true)
    private UUID familyId;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false, length = 50)
    private PlanType planType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private SubscriptionStatus status;

    @Column(name = "base_members_allowed", nullable = false)
    private int baseMembersAllowed;

    @Column(name = "extra_members_paid", nullable = false)
    private int extraMembersPaid;

    @Column(name = "price_monthly", nullable = false, precision = 15, scale = 2)
    private BigDecimal priceMonthly;

    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;

    @Column(name = "current_period_starts_at", nullable = false)
    private LocalDateTime currentPeriodStartsAt;

    @Column(name = "current_period_ends_at")
    private LocalDateTime currentPeriodEndsAt;

    @Column(name = "grace_days_remaining", nullable = false)
    private int graceDaysRemaining = 5;

    @Column(name = "auto_renew", nullable = false)
    private boolean autoRenew = true;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getFamilyId() {
        return familyId;
    }

    public void setFamilyId(UUID familyId) {
        this.familyId = familyId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
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
}
