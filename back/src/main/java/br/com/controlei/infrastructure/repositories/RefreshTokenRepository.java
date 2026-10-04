package br.com.controlei.infrastructure.repositories;

import br.com.controlei.infrastructure.persistence.entities.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByToken(String token);

    // clearAutomatically: o UPDATE em lote passa por fora do cache de persistencia; sem limpar, uma leitura
    // seguinte na mesma transacao ainda veria o token como nao revogado (revogacao "invisivel").
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE RefreshTokenEntity r SET r.revoked = true WHERE r.userId = :userId")
    void revokeAllByUserId(UUID userId);
}
