package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.InvestmentRepositoryPort;
import br.com.controlei.domain.models.entities.Investment;
import br.com.controlei.domain.models.enums.InvestmentType;
import br.com.controlei.infrastructure.mappers.InvestmentEntityMapper;
import br.com.controlei.infrastructure.persistence.entities.InvestmentEntity;
import br.com.controlei.infrastructure.repositories.InvestmentRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class InvestmentRepositoryAdapter implements InvestmentRepositoryPort {

    private final InvestmentRepository repository;
    private final InvestmentEntityMapper mapper;

    public InvestmentRepositoryAdapter(InvestmentRepository repository, InvestmentEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Investment> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public List<Investment> findAllByFamilyIdAndFilters(UUID familyId, UUID userId, InvestmentType type,
                                                         UUID categoryId, LocalDate startDate, LocalDate endDate) {
        Specification<InvestmentEntity> spec = Specification.where(InvestmentRepository.byFamilyId(familyId))
                .and(InvestmentRepository.deletedAtIsNull())
                .and(InvestmentRepository.byUserId(userId))
                .and(InvestmentRepository.byType(type))
                .and(InvestmentRepository.byCategoryId(categoryId))
                .and(InvestmentRepository.referenceDateBetween(startDate, endDate));
        return repository.findAll(spec).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Investment save(Investment investment) {
        InvestmentEntity entity = mapper.toEntity(investment);
        InvestmentEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }
}
