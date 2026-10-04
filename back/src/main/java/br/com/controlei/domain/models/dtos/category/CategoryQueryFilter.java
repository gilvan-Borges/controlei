package br.com.controlei.domain.models.dtos.category;

import br.com.controlei.domain.models.enums.CategoryType;

public record CategoryQueryFilter(
        Boolean active,
        CategoryType type
) {
}
