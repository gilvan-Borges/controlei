package br.com.controlei.application.mappers;

import br.com.controlei.domain.models.dtos.installment.InstallmentResponse;
import br.com.controlei.domain.models.entities.Installment;
import org.springframework.stereotype.Component;

@Component
public class InstallmentMapper {

    public InstallmentResponse toResponse(Installment installment) {
        return new InstallmentResponse(
                installment.getId(),
                installment.getFamilyId(),
                installment.getUserId(),
                installment.getDebtId(),
                installment.getInstallmentNumber(),
                installment.getAmount(),
                installment.getDueDate(),
                installment.getPaidAt(),
                installment.getStatus(),
                installment.getCreatedAt(),
                installment.getUpdatedAt()
        );
    }
}
