package br.com.controlei.domain.models.dtos.category;

import br.com.controlei.domain.models.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
        @NotBlank(message = "Nome e obrigatorio")
        @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
        String name,

        @NotNull(message = "Tipo e obrigatorio")
        CategoryType type,

        @Size(max = 50, message = "Cor deve ter no maximo 50 caracteres")
        String color,

        @Size(max = 255, message = "Icone deve ter no maximo 255 caracteres")
        String icon,

        Boolean active
) {
}
