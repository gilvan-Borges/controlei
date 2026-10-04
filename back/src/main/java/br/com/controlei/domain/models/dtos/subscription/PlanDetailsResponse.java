package br.com.controlei.domain.models.dtos.subscription;

import br.com.controlei.domain.models.enums.PlanType;

import java.math.BigDecimal;
import java.util.List;

public record PlanDetailsResponse(
        PlanType planType,
        String name,
        String description,
        BigDecimal priceMonthly,
        int baseMembersIncluded,
        BigDecimal extraMemberPriceMonthly,
        boolean allowsExtraMembers,
        List<String> features
) {}
