package br.com.controlei.domain.models.dtos.account;

import br.com.controlei.domain.models.enums.AccountType;

import java.util.UUID;

public record AccountQueryFilter(
        Boolean active,
        AccountType type,
        Boolean shared,
        UUID userId
) {
}
