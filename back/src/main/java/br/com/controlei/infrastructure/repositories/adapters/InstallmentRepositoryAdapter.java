package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.InstallmentRepositoryPort;
import br.com.controlei.domain.models.entities.Installment;
import br.com.controlei.domain.models.enums.InstallmentStatus;
import br.com.controlei.infrastructure.mappers.InstallmentEntityMapper;
import br.com.controlei.infrastructure.persistence.entities.InstallmentEntity;
import br.com.controlei.infrastructure.repositories.InstallmentRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class InstallmentRepositoryAdapter implements InstallmentRepositoryPort {

    private final InstallmentRepository repository;
    private final InstallmentEntityMapper mapper;

    public InstallmentRepositoryAdapter(InstallmentRepository repository, InstallmentEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Installment> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public List<Installment> findAllByDebtIdAndDeletedAtIsNullOrderByInstallmentNumberAsc(UUID debtId) {
        return repository.findAllByDebtIdAndDeletedAtIsNullOrderByInstallmentNumberAsc(debtId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Installment> findAllByFamilyIdAndFilters(UUID familyId, UUID userId, UUID debtId,
                                                         InstallmentStatus status,
                                                         LocalDate startDate, LocalDate endDate) {
        Specification<InstallmentEntity> spec = Specification.where(InstallmentRepository.byFamilyId(familyId))
                .and(InstallmentRepository.deletedAtIsNull())
                .and(InstallmentRepository.byUserId(userId))
                .and(InstallmentRepository.byDebtId(debtId))
                .and(InstallmentRepository.byStatus(status))
                .and(InstallmentRepository.dueDateBetween(startDate, endDate));
        return repository.findAll(spec).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Installment> saveAll(List<Installment> installments) {
        List<InstallmentEntity> entities = installments.stream()
                .map(mapper::toEntity)
                .toList();
        List<InstallmentEntity> saved = repository.saveAll(entities);
        return saved.stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Installment save(Installment installment) {
        InstallmentEntity entity = mapper.toEntity(installment);
        InstallmentEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }
}
