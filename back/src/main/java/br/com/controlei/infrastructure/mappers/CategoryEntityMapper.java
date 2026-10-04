package br.com.controlei.infrastructure.mappers;

import br.com.controlei.domain.models.entities.Category;
import br.com.controlei.infrastructure.persistence.entities.CategoryEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoryEntityMapper {

    public Category toDomain(CategoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Category(
                entity.getId(),
                entity.getFamilyId(),
                entity.getName(),
                entity.getType(),
                entity.getColor(),
                entity.getIcon(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public CategoryEntity toEntity(Category domain) {
        if (domain == null) {
            return null;
        }
        CategoryEntity entity = new CategoryEntity();
        entity.setId(domain.getId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setName(domain.getName());
        entity.setType(domain.getType());
        entity.setColor(domain.getColor());
        entity.setIcon(domain.getIcon());
        entity.setActive(domain.isActive());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());
        return entity;
    }
}
