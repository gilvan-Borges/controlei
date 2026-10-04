package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.AttachmentRepositoryPort;
import br.com.controlei.domain.models.entities.Attachment;
import br.com.controlei.infrastructure.mappers.AttachmentEntityMapper;
import br.com.controlei.infrastructure.repositories.AttachmentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AttachmentRepositoryAdapter implements AttachmentRepositoryPort {

    private final AttachmentRepository repository;
    private final AttachmentEntityMapper mapper;

    public AttachmentRepositoryAdapter(AttachmentRepository repository, AttachmentEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Attachment save(Attachment attachment) {
        return mapper.toDomain(repository.save(mapper.toEntity(attachment)));
    }

    @Override
    public Optional<Attachment> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public List<Attachment> findAllByFamilyIdAndDeletedAtIsNull(UUID familyId) {
        return repository.findAllByFamilyIdAndDeletedAtIsNull(familyId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
