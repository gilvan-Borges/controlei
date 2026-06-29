package br.com.controlei.application.mappers;

import br.com.controlei.domain.models.dtos.debt.CreateDebtRequest;
import br.com.controlei.domain.models.dtos.debt.DebtResponse;
import br.com.controlei.domain.models.dtos.debt.UpdateDebtRequest;
import br.com.controlei.domain.models.entities.Debt;
import br.com.controlei.domain.models.enums.DebtStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class DebtMapper {

    public Debt toEntity(CreateDebtRequest request, UUID familyId, BigDecimal installmentAmount) {
        return new Debt(
                UUID.randomUUID(),
                familyId,
                request.userId(),
                request.categoryId(),
                request.description(),
                request.purchaseDate(),
                request.totalAmount(),
                request.installmentCount(),
                installmentAmount,
                DebtStatus.PENDING,
                request.notes(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null
        );
    }

    public DebtResponse toResponse(Debt debt) {
        return new DebtResponse(
                debt.getId(),
                debt.getFamilyId(),
                debt.getUserId(),
                debt.getCategoryId(),
                debt.getDescription(),
                debt.getPurchaseDate(),
                debt.getTotalAmount(),
                debt.getInstallmentCount(),
                debt.getInstallmentAmount(),
                debt.getStatus(),
                debt.getNotes(),
                debt.getCreatedAt(),
                debt.getUpdatedAt()
        );
    }

    public void updateEntity(Debt debt, UpdateDebtRequest request) {
        debt.setCategoryId(request.categoryId());
        debt.setDescription(request.description());
        debt.setPurchaseDate(request.purchaseDate());
        debt.setNotes(request.notes());
        debt.setUpdatedAt(LocalDateTime.now());
    }
}
