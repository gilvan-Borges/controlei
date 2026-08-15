package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.RefreshTokenRepositoryPort;
import br.com.controlei.domain.models.entities.RefreshToken;
import br.com.controlei.infrastructure.mappers.RefreshTokenEntityMapper;
import br.com.controlei.infrastructure.repositories.RefreshTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

    private final RefreshTokenRepository repository;
    private final RefreshTokenEntityMapper mapper;

    public RefreshTokenRepositoryAdapter(RefreshTokenRepository repository, RefreshTokenEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public RefreshToken save(RefreshToken refreshToken) {
        return mapper.toDomain(repository.save(mapper.toEntity(refreshToken)));
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return repository.findByToken(token).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void revokeAllByUserId(UUID userId) {
        repository.revokeAllByUserId(userId);
    }
}
