package br.com.controlei.application.services;

import br.com.controlei.application.exceptions.BusinessException;
import br.com.controlei.application.exceptions.NotFoundException;
import br.com.controlei.application.mappers.CategoryMapper;
import br.com.controlei.domain.contracts.repositories.CategoryRepositoryPort;
import br.com.controlei.domain.models.dtos.category.CategoryQueryFilter;
import br.com.controlei.domain.models.dtos.category.CategoryResponse;
import br.com.controlei.domain.models.dtos.category.CreateCategoryRequest;
import br.com.controlei.domain.models.dtos.category.UpdateCategoryRequest;
import br.com.controlei.domain.models.entities.Category;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepositoryPort categoryRepository;
    private final CategoryMapper categoryMapper;
    private final AuthorizationService authorizationService;

    public CategoryService(CategoryRepositoryPort categoryRepository,
                           CategoryMapper categoryMapper,
                           AuthorizationService authorizationService) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.authorizationService = authorizationService;
    }

    public List<CategoryResponse> listCategories(CategoryQueryFilter filter) {
        UUID familyId = authorizationService.currentFamilyId();
        return categoryRepository.findAllByFamilyIdAndFilters(familyId, filter.active(), filter.type())
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        UUID familyId = authorizationService.currentFamilyId();

        if (categoryRepository.existsByFamilyIdAndTypeAndNameAndDeletedAtIsNull(
                familyId, request.type(), request.name())) {
            throw new BusinessException("Categoria ja existe para este tipo na familia");
        }

        Category category = categoryMapper.toEntity(request, familyId);
        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    public CategoryResponse getCategory(UUID id) {
        Category category = findActiveById(id);
        authorizationService.requireSameFamily(category.getFamilyId());
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request) {
        Category category = findActiveById(id);
        authorizationService.requireSameFamily(category.getFamilyId());

        if (!category.getName().equalsIgnoreCase(request.name()) || category.getType() != request.type()) {
            if (categoryRepository.existsByFamilyIdAndTypeAndNameAndDeletedAtIsNull(
                    category.getFamilyId(), request.type(), request.name())) {
                throw new BusinessException("Categoria ja existe para este tipo na familia");
            }
        }

        categoryMapper.updateEntity(category, request);
        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    @Transactional
    public void deleteCategory(UUID id) {
        Category category = findActiveById(id);
        authorizationService.requireSameFamily(category.getFamilyId());
        category.setDeletedAt(java.time.LocalDateTime.now());
        category.setDeletedBy(authorizationService.currentUserId().toString());
        categoryRepository.save(category);
    }

    private Category findActiveById(UUID id) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Categoria nao encontrada"));
        if (!category.isActive()) {
            throw new NotFoundException("Categoria nao encontrada");
        }
        return category;
    }
}
