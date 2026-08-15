package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.SplitSettlementRepositoryPort;
import br.com.controlei.domain.models.entities.SplitSettlement;
import br.com.controlei.infrastructure.mappers.SplitSettlementEntityMapper;
import br.com.controlei.infrastructure.repositories.SplitSettlementRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SplitSettlementRepositoryAdapter implements SplitSettlementRepositoryPort {

    private final SplitSettlementRepository repository;
    private final SplitSettlementEntityMapper mapper;

    public SplitSettlementRepositoryAdapter(SplitSettlementRepository repository,
                                           SplitSettlementEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public SplitSettlement save(SplitSettlement settlement) {
        return mapper.toDomain(repository.save(mapper.toEntity(settlement)));
    }

    @Override
    public Optional<SplitSettlement> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public List<SplitSettlement> findAllByFamilyIdAndDeletedAtIsNullOrderBySettlementDateDescCreatedAtDesc(UUID familyId) {
        return repository.findAllByFamilyIdAndDeletedAtIsNullOrderBySettlementDateDescCreatedAtDesc(familyId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
