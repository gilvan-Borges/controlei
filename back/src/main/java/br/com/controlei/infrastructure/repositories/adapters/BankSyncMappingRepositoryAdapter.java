package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.BankSyncMappingRepositoryPort;
import br.com.controlei.domain.models.entities.BankSyncMapping;
import br.com.controlei.infrastructure.mappers.BankSyncMappingEntityMapper;
import br.com.controlei.infrastructure.repositories.BankSyncMappingRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class BankSyncMappingRepositoryAdapter implements BankSyncMappingRepositoryPort {

    private final BankSyncMappingRepository repository;
    private final BankSyncMappingEntityMapper mapper;

    public BankSyncMappingRepositoryAdapter(BankSyncMappingRepository repository,
                                           BankSyncMappingEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public BankSyncMapping save(BankSyncMapping mapping) {
        return mapper.toDomain(repository.save(mapper.toEntity(mapping)));
    }

    @Override
    public Optional<BankSyncMapping> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public List<BankSyncMapping> findAllByBankConnectionIdAndDeletedAtIsNull(UUID bankConnectionId) {
        return repository.findAllByBankConnectionIdAndDeletedAtIsNull(bankConnectionId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
