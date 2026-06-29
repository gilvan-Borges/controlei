package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.entities.Category;
import br.com.controlei.domain.models.enums.CategoryType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepositoryPort {

    Optional<Category> findByIdAndDeletedAtIsNull(UUID id);

    List<Category> findAllByFamilyIdAndDeletedAtIsNull(UUID familyId);

    List<Category> findAllByFamilyIdAndFilters(UUID familyId, Boolean active, CategoryType type);

    boolean existsByFamilyIdAndTypeAndNameAndDeletedAtIsNull(UUID familyId, CategoryType type, String name);

    Category save(Category category);
}
