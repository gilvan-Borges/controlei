package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.SubscriptionRepositoryPort;
import br.com.controlei.domain.models.entities.Subscription;
import br.com.controlei.infrastructure.mappers.SubscriptionEntityMapper;
import br.com.controlei.infrastructure.repositories.springdata.SpringDataSubscriptionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class SubscriptionRepositoryAdapter implements SubscriptionRepositoryPort {

    private final SpringDataSubscriptionRepository repository;
    private final SubscriptionEntityMapper mapper;

    public SubscriptionRepositoryAdapter(SpringDataSubscriptionRepository repository,
                                       SubscriptionEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Subscription save(Subscription subscription) {
        return mapper.toDomain(repository.save(mapper.toEntity(subscription)));
    }

    @Override
    public Optional<Subscription> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Subscription> findByFamilyIdAndDeletedAtIsNull(UUID familyId) {
        return repository.findByFamilyIdAndDeletedAtIsNull(familyId).map(mapper::toDomain);
    }

    @Override
    public boolean existsByFamilyIdAndDeletedAtIsNull(UUID familyId) {
        return repository.existsByFamilyIdAndDeletedAtIsNull(familyId);
    }
}
