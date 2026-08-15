package br.com.controlei.domain.models.dtos.split;

import java.util.List;

public record FamilyBalanceResponse(
        List<MemberBalance> memberBalances,
        List<SuggestedSettlement> suggestedSettlements
) {
}
