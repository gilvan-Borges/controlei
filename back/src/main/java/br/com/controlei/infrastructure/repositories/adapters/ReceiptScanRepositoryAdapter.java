package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.ReceiptScanRepositoryPort;
import br.com.controlei.domain.models.entities.ReceiptScan;
import br.com.controlei.infrastructure.mappers.ReceiptScanEntityMapper;
import br.com.controlei.infrastructure.repositories.ReceiptScanRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ReceiptScanRepositoryAdapter implements ReceiptScanRepositoryPort {

    private final ReceiptScanRepository repository;
    private final ReceiptScanEntityMapper mapper;

    public ReceiptScanRepositoryAdapter(ReceiptScanRepository repository, ReceiptScanEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public ReceiptScan save(ReceiptScan scan) {
        return mapper.toDomain(repository.save(mapper.toEntity(scan)));
    }

    @Override
    public Optional<ReceiptScan> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public List<ReceiptScan> findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId) {
        return repository.findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(familyId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
