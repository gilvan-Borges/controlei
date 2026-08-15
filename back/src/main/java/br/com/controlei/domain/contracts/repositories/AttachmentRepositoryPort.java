package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.entities.Attachment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttachmentRepositoryPort {

    Attachment save(Attachment attachment);

    Optional<Attachment> findByIdAndDeletedAtIsNull(UUID id);

    List<Attachment> findAllByFamilyIdAndDeletedAtIsNull(UUID familyId);
}
