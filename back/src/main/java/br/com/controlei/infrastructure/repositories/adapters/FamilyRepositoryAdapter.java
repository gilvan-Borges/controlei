package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.FamilyRepositoryPort;
import br.com.controlei.domain.models.entities.Family;
import br.com.controlei.infrastructure.mappers.FamilyEntityMapper;
import br.com.controlei.infrastructure.repositories.FamilyRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class FamilyRepositoryAdapter implements FamilyRepositoryPort {

    private final FamilyRepository repository;
    private final FamilyEntityMapper mapper;

    public FamilyRepositoryAdapter(FamilyRepository repository, FamilyEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Family> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public Family save(Family family) {
        return mapper.toDomain(repository.save(mapper.toEntity(family)));
    }
}
