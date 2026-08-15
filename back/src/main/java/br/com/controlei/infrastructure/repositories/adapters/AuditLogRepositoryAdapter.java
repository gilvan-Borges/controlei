package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.AuditLogRepositoryPort;
import br.com.controlei.domain.models.entities.AuditLog;
import br.com.controlei.infrastructure.mappers.AuditLogEntityMapper;
import br.com.controlei.infrastructure.repositories.AuditLogRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AuditLogRepositoryAdapter implements AuditLogRepositoryPort {

    private final AuditLogRepository repository;
    private final AuditLogEntityMapper mapper;

    public AuditLogRepositoryAdapter(AuditLogRepository repository, AuditLogEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public AuditLog save(AuditLog log) {
        return mapper.toDomain(repository.save(mapper.toEntity(log)));
    }

    @Override
    public Optional<AuditLog> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public List<AuditLog> findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId) {
        return repository.findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(familyId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
