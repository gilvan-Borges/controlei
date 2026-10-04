package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.NotificationRepositoryPort;
import br.com.controlei.domain.models.entities.Notification;
import br.com.controlei.infrastructure.mappers.NotificationEntityMapper;
import br.com.controlei.infrastructure.repositories.NotificationRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class NotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final NotificationRepository repository;
    private final NotificationEntityMapper mapper;

    public NotificationRepositoryAdapter(NotificationRepository repository, NotificationEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Notification save(Notification notification) {
        return mapper.toDomain(repository.save(mapper.toEntity(notification)));
    }

    @Override
    public Optional<Notification> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public List<Notification> findAllByFamilyIdAndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId, UUID userId) {
        return repository.findAllByFamilyIdAndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(familyId, userId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Notification> findAllByFamilyIdAndUserIdAndReadIsFalseAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId, UUID userId) {
        return repository.findAllByFamilyIdAndUserIdAndReadIsFalseAndDeletedAtIsNullOrderByCreatedAtDesc(familyId, userId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countByFamilyIdAndUserIdAndReadIsFalseAndDeletedAtIsNull(UUID familyId, UUID userId) {
        return repository.countByFamilyIdAndUserIdAndReadIsFalseAndDeletedAtIsNull(familyId, userId);
    }
}
