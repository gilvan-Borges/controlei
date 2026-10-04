package br.com.controlei.application.services;

import br.com.controlei.application.exceptions.BusinessException;
import br.com.controlei.application.exceptions.NotFoundException;
import br.com.controlei.application.mappers.InstallmentMapper;
import br.com.controlei.domain.contracts.repositories.InstallmentRepositoryPort;
import br.com.controlei.domain.models.dtos.installment.InstallmentResponse;
import br.com.controlei.domain.models.dtos.installment.PayInstallmentResponse;
import br.com.controlei.domain.models.dtos.debt.DebtResponse;
import br.com.controlei.domain.models.entities.Installment;
import br.com.controlei.domain.models.enums.InstallmentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class InstallmentService {

    private final InstallmentRepositoryPort installmentRepository;
    private final InstallmentMapper installmentMapper;
    private final DebtService debtService;
    private final AuthorizationService authorizationService;

    public InstallmentService(InstallmentRepositoryPort installmentRepository,
                              InstallmentMapper installmentMapper,
                              DebtService debtService,
                              AuthorizationService authorizationService) {
        this.installmentRepository = installmentRepository;
        this.installmentMapper = installmentMapper;
        this.debtService = debtService;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public PayInstallmentResponse payInstallment(UUID id) {
        Installment installment = findActiveById(id);
        authorizationService.requireCanWrite(installment.getFamilyId(), installment.getUserId());

        if (installment.getStatus() == InstallmentStatus.CANCELED) {
            throw new BusinessException("Parcela cancelada nao pode ser paga");
        }

        installment.setStatus(InstallmentStatus.PAID);
        installment.setPaidAt(LocalDateTime.now());
        installment.setUpdatedAt(LocalDateTime.now());
        Installment saved = installmentRepository.save(installment);

        debtService.recalculateDebtStatus(installment.getDebtId());

        InstallmentResponse installmentResponse = installmentMapper.toResponse(saved);
        DebtResponse debtResponse = debtService.getDebt(installment.getDebtId());

        return new PayInstallmentResponse(installmentResponse, debtResponse);
    }

    @Transactional
    public PayInstallmentResponse cancelInstallment(UUID id) {
        Installment installment = findActiveById(id);
        authorizationService.requireCanWrite(installment.getFamilyId(), installment.getUserId());

        if (installment.getStatus() == InstallmentStatus.PAID) {
            throw new BusinessException("Parcela paga nao pode ser cancelada");
        }

        installment.setStatus(InstallmentStatus.CANCELED);
        installment.setUpdatedAt(LocalDateTime.now());
        Installment saved = installmentRepository.save(installment);

        debtService.recalculateDebtStatus(installment.getDebtId());

        InstallmentResponse installmentResponse = installmentMapper.toResponse(saved);
        DebtResponse debtResponse = debtService.getDebt(installment.getDebtId());

        return new PayInstallmentResponse(installmentResponse, debtResponse);
    }

    private Installment findActiveById(UUID id) {
        return installmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Parcela nao encontrada"));
    }
}
