package br.com.controlei.application.mappers;

import br.com.controlei.domain.models.dtos.category.CategoryResponse;
import br.com.controlei.domain.models.dtos.category.CreateCategoryRequest;
import br.com.controlei.domain.models.dtos.category.UpdateCategoryRequest;
import br.com.controlei.domain.models.entities.Category;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class CategoryMapper {

    public Category toEntity(CreateCategoryRequest request, UUID familyId) {
        return new Category(
                UUID.randomUUID(),
                familyId,
                request.name(),
                request.type(),
                request.color(),
                request.icon(),
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null
        );
    }

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getFamilyId(),
                category.getName(),
                category.getType(),
                category.getColor(),
                category.getIcon(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    public void updateEntity(Category category, UpdateCategoryRequest request) {
        category.setName(request.name());
        category.setType(request.type());
        category.setColor(request.color());
        category.setIcon(request.icon());
        if (request.active() != null) {
            category.setActive(request.active());
        }
        category.setUpdatedAt(LocalDateTime.now());
    }
}
