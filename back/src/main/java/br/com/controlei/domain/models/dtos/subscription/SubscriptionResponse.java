package br.com.controlei.domain.models.dtos.subscription;

import br.com.controlei.domain.models.enums.PlanType;
import br.com.controlei.domain.models.enums.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SubscriptionResponse(
        UUID id,
        UUID familyId,
        UUID ownerId,
        PlanType planType,
        SubscriptionStatus status,
        int baseMembersAllowed,
        int extraMembersPaid,
        int totalMembersAllowed,
        BigDecimal priceMonthly,
        LocalDateTime trialEndsAt,
        LocalDateTime currentPeriodStartsAt,
        LocalDateTime currentPeriodEndsAt,
        long daysRemaining,
        boolean isTrial,
        boolean isReadOnly,
        boolean autoRenew,
        String paymentMethod
) {}
