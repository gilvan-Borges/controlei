package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.DebtRepositoryPort;
import br.com.controlei.domain.models.entities.Debt;
import br.com.controlei.domain.models.enums.DebtStatus;
import br.com.controlei.infrastructure.mappers.DebtEntityMapper;
import br.com.controlei.infrastructure.persistence.entities.DebtEntity;
import br.com.controlei.infrastructure.repositories.DebtRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DebtRepositoryAdapter implements DebtRepositoryPort {

    private final DebtRepository repository;
    private final DebtEntityMapper mapper;

    public DebtRepositoryAdapter(DebtRepository repository, DebtEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Debt> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public List<Debt> findAllByFamilyIdAndFilters(UUID familyId, UUID userId, DebtStatus status) {
        Specification<DebtEntity> spec = Specification.where(DebtRepository.byFamilyId(familyId))
                .and(DebtRepository.deletedAtIsNull())
                .and(DebtRepository.byUserId(userId))
                .and(DebtRepository.byStatus(status));
        return repository.findAll(spec).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Debt save(Debt debt) {
        DebtEntity entity = mapper.toEntity(debt);
        DebtEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }
}
