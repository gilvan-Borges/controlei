package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.BankConnectionRepositoryPort;
import br.com.controlei.domain.models.entities.BankConnection;
import br.com.controlei.infrastructure.mappers.BankConnectionEntityMapper;
import br.com.controlei.infrastructure.repositories.BankConnectionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class BankConnectionRepositoryAdapter implements BankConnectionRepositoryPort {

    private final BankConnectionRepository repository;
    private final BankConnectionEntityMapper mapper;

    public BankConnectionRepositoryAdapter(BankConnectionRepository repository, BankConnectionEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public BankConnection save(BankConnection connection) {
        return mapper.toDomain(repository.save(mapper.toEntity(connection)));
    }

    @Override
    public Optional<BankConnection> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public Optional<BankConnection> findByExternalItemIdAndDeletedAtIsNull(String externalItemId) {
        return repository.findByExternalItemIdAndDeletedAtIsNull(externalItemId).map(mapper::toDomain);
    }

    @Override
    public List<BankConnection> findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId) {
        return repository.findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(familyId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
