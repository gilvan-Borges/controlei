package br.com.controlei.infrastructure.repositories;

import br.com.controlei.infrastructure.persistence.entities.AttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<AttachmentEntity, UUID> {

    Optional<AttachmentEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<AttachmentEntity> findAllByFamilyIdAndDeletedAtIsNull(UUID familyId);
}
