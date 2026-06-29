package br.com.controlei.application.services;

import br.com.controlei.application.exceptions.BusinessException;
import br.com.controlei.application.exceptions.NotFoundException;
import br.com.controlei.application.mappers.DebtMapper;
import br.com.controlei.application.mappers.InstallmentMapper;
import br.com.controlei.domain.contracts.repositories.CategoryRepositoryPort;
import br.com.controlei.domain.contracts.repositories.DebtRepositoryPort;
import br.com.controlei.domain.contracts.repositories.InstallmentRepositoryPort;
import br.com.controlei.domain.models.dtos.debt.CreateDebtRequest;
import br.com.controlei.domain.models.dtos.debt.DebtQueryFilter;
import br.com.controlei.domain.models.dtos.debt.DebtResponse;
import br.com.controlei.domain.models.dtos.debt.UpdateDebtRequest;
import br.com.controlei.domain.models.dtos.installment.InstallmentResponse;
import br.com.controlei.domain.models.entities.Category;
import br.com.controlei.domain.models.entities.Debt;
import br.com.controlei.domain.models.entities.Installment;
import br.com.controlei.domain.models.enums.CategoryType;
import br.com.controlei.domain.models.enums.DebtStatus;
import br.com.controlei.domain.models.enums.InstallmentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DebtService {

    private final DebtRepositoryPort debtRepository;
    private final InstallmentRepositoryPort installmentRepository;
    private final CategoryRepositoryPort categoryRepository;
    private final DebtMapper debtMapper;
    private final InstallmentMapper installmentMapper;
    private final AuthorizationService authorizationService;

    public DebtService(DebtRepositoryPort debtRepository,
                       InstallmentRepositoryPort installmentRepository,
                       CategoryRepositoryPort categoryRepository,
                       DebtMapper debtMapper,
                       InstallmentMapper installmentMapper,
                       AuthorizationService authorizationService) {
        this.debtRepository = debtRepository;
        this.installmentRepository = installmentRepository;
        this.categoryRepository = categoryRepository;
        this.debtMapper = debtMapper;
        this.installmentMapper = installmentMapper;
        this.authorizationService = authorizationService;
    }

    public List<DebtResponse> listDebts(DebtQueryFilter filter) {
        UUID familyId = authorizationService.currentFamilyId();
        return debtRepository.findAllByFamilyIdAndFilters(familyId, filter.userId(), filter.status())
                .stream()
                .map(debtMapper::toResponse)
                .toList();
    }

    @Transactional
    public DebtResponse createDebt(CreateDebtRequest request) {
        UUID familyId = authorizationService.currentFamilyId();

        authorizationService.requireCanWrite(familyId, request.userId());

        if (request.categoryId() != null) {
            Category category = categoryRepository.findByIdAndDeletedAtIsNull(request.categoryId())
                    .orElseThrow(() -> new NotFoundException("Categoria nao encontrada"));
            if (!category.getFamilyId().equals(familyId)) {
                throw new NotFoundException("Categoria nao encontrada");
            }
            if (category.getType() != CategoryType.DEBT) {
                throw new BusinessException("Categoria deve ser do tipo DEBITO");
            }
        }

        BigDecimal installmentAmount = request.totalAmount()
                .divide(BigDecimal.valueOf(request.installmentCount()), 4, RoundingMode.FLOOR);

        Debt debt = debtMapper.toEntity(request, familyId, installmentAmount);
        Debt savedDebt = debtRepository.save(debt);

        List<Installment> installments = generateInstallments(savedDebt, request);
        installmentRepository.saveAll(installments);

        return debtMapper.toResponse(savedDebt);
    }

    public DebtResponse getDebt(UUID id) {
        Debt debt = findActiveById(id);
        authorizationService.requireSameFamily(debt.getFamilyId());
        return debtMapper.toResponse(debt);
    }

    @Transactional
    public DebtResponse updateDebt(UUID id, UpdateDebtRequest request) {
        Debt debt = findActiveById(id);
        authorizationService.requireCanWrite(debt.getFamilyId(), debt.getUserId());

        if (request.categoryId() != null) {
            Category category = categoryRepository.findByIdAndDeletedAtIsNull(request.categoryId())
                    .orElseThrow(() -> new NotFoundException("Categoria nao encontrada"));
            if (!category.getFamilyId().equals(debt.getFamilyId())) {
                throw new NotFoundException("Categoria nao encontrada");
            }
            if (category.getType() != CategoryType.DEBT) {
                throw new BusinessException("Categoria deve ser do tipo DEBITO");
            }
        }

        debtMapper.updateEntity(debt, request);
        Debt saved = debtRepository.save(debt);
        return debtMapper.toResponse(saved);
    }

    @Transactional
    public void deleteDebt(UUID id) {
        Debt debt = findActiveById(id);
        authorizationService.requireCanWrite(debt.getFamilyId(), debt.getUserId());
        debt.setDeletedAt(LocalDateTime.now());
        debt.setDeletedBy(authorizationService.currentUserId().toString());
        debtRepository.save(debt);
    }

    public List<InstallmentResponse> listInstallmentsByDebt(UUID debtId) {
        Debt debt = findActiveById(debtId);
        authorizationService.requireSameFamily(debt.getFamilyId());
        return installmentRepository.findAllByDebtIdAndDeletedAtIsNullOrderByInstallmentNumberAsc(debtId)
                .stream()
                .map(installmentMapper::toResponse)
                .toList();
    }

    public List<InstallmentResponse> listInstallments(UUID userId, UUID debtId, InstallmentStatus status,
                                                      LocalDate startDate, LocalDate endDate) {
        UUID familyId = authorizationService.currentFamilyId();
        return installmentRepository.findAllByFamilyIdAndFilters(familyId, userId, debtId, status, startDate, endDate)
                .stream()
                .map(installmentMapper::toResponse)
                .toList();
    }

    @Transactional
    public void recalculateDebtStatus(UUID debtId) {
        Debt debt = debtRepository.findByIdAndDeletedAtIsNull(debtId)
                .orElseThrow(() -> new NotFoundException("Divida nao encontrada"));

        List<Installment> installments = installmentRepository.findAllByDebtIdAndDeletedAtIsNullOrderByInstallmentNumberAsc(debtId);

        boolean allPaid = installments.stream()
                .allMatch(i -> i.getStatus() == InstallmentStatus.PAID);
        boolean allCanceled = installments.stream()
                .allMatch(i -> i.getStatus() == InstallmentStatus.CANCELED);

        if (allPaid) {
            debt.setStatus(DebtStatus.PAID);
        } else if (allCanceled) {
            debt.setStatus(DebtStatus.CANCELED);
        } else {
            debt.setStatus(DebtStatus.PENDING);
        }

        debt.setUpdatedAt(LocalDateTime.now());
        debtRepository.save(debt);
    }

    private List<Installment> generateInstallments(Debt debt, CreateDebtRequest request) {
        List<Installment> installments = new ArrayList<>();
        BigDecimal totalCalculated = BigDecimal.ZERO;
        LocalDate dueDate = request.firstDueDate();

        for (int i = 1; i <= request.installmentCount(); i++) {
            BigDecimal amount = debt.getInstallmentAmount();
            totalCalculated = totalCalculated.add(amount);

            if (i == request.installmentCount()) {
                BigDecimal remainder = request.totalAmount().subtract(totalCalculated.subtract(amount));
                amount = remainder;
            }

            Installment installment = new Installment(
                    UUID.randomUUID(),
                    debt.getFamilyId(),
                    debt.getUserId(),
                    debt.getId(),
                    i,
                    amount,
                    dueDate,
                    null,
                    InstallmentStatus.PENDING,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    null,
                    null
            );
            installments.add(installment);
            dueDate = dueDate.plusMonths(1);
        }

        return installments;
    }

    private Debt findActiveById(UUID id) {
        return debtRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Divida nao encontrada"));
    }
}
