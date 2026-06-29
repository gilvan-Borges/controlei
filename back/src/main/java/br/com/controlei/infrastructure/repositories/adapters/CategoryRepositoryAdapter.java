package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.CategoryRepositoryPort;
import br.com.controlei.domain.models.entities.Category;
import br.com.controlei.domain.models.enums.CategoryType;
import br.com.controlei.infrastructure.mappers.CategoryEntityMapper;
import br.com.controlei.infrastructure.persistence.entities.CategoryEntity;
import br.com.controlei.infrastructure.repositories.CategoryRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CategoryRepositoryAdapter implements CategoryRepositoryPort {

    private final CategoryRepository repository;
    private final CategoryEntityMapper mapper;

    public CategoryRepositoryAdapter(CategoryRepository repository, CategoryEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Category> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public List<Category> findAllByFamilyIdAndDeletedAtIsNull(UUID familyId) {
        return repository.findAllByFamilyIdAndDeletedAtIsNull(familyId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Category> findAllByFamilyIdAndFilters(UUID familyId, Boolean active, CategoryType type) {
        Specification<CategoryEntity> spec = Specification.where(CategoryRepository.byFamilyId(familyId))
                .and(CategoryRepository.deletedAtIsNull())
                .and(CategoryRepository.byActive(active))
                .and(CategoryRepository.byType(type));
        return repository.findAll(spec).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByFamilyIdAndTypeAndNameAndDeletedAtIsNull(UUID familyId, CategoryType type, String name) {
        return repository.existsByFamilyIdAndTypeAndNameAndDeletedAtIsNull(familyId, type, name);
    }

    @Override
    public Category save(Category category) {
        CategoryEntity entity = mapper.toEntity(category);
        CategoryEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }
}
