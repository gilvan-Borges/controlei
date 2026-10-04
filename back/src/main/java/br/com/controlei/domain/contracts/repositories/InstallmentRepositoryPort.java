package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.entities.Installment;
import br.com.controlei.domain.models.enums.InstallmentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstallmentRepositoryPort {

    Optional<Installment> findByIdAndDeletedAtIsNull(UUID id);

    List<Installment> findAllByDebtIdAndDeletedAtIsNullOrderByInstallmentNumberAsc(UUID debtId);

    List<Installment> findAllByFamilyIdAndFilters(UUID familyId, UUID userId, UUID debtId,
                                                  InstallmentStatus status,
                                                  LocalDate startDate, LocalDate endDate);

    List<Installment> saveAll(List<Installment> installments);

    Installment save(Installment installment);
}
