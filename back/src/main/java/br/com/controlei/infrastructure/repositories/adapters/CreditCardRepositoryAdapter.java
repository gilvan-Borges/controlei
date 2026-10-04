package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.CreditCardRepositoryPort;
import br.com.controlei.domain.models.entities.CreditCard;
import br.com.controlei.infrastructure.mappers.CreditCardEntityMapper;
import br.com.controlei.infrastructure.repositories.CreditCardRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CreditCardRepositoryAdapter implements CreditCardRepositoryPort {

    private final CreditCardRepository repository;
    private final CreditCardEntityMapper mapper;

    public CreditCardRepositoryAdapter(CreditCardRepository repository, CreditCardEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public CreditCard save(CreditCard creditCard) {
        return mapper.toDomain(repository.save(mapper.toEntity(creditCard)));
    }

    @Override
    public Optional<CreditCard> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public List<CreditCard> findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId) {
        return repository.findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(familyId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<CreditCard> findAllByFamilyIdAndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId, UUID userId) {
        return repository.findAllByFamilyIdAndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(familyId, userId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
