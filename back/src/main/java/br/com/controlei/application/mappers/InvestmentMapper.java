package br.com.controlei.application.mappers;

import br.com.controlei.domain.models.dtos.investment.CreateInvestmentRequest;
import br.com.controlei.domain.models.dtos.investment.InvestmentResponse;
import br.com.controlei.domain.models.dtos.investment.UpdateInvestmentRequest;
import br.com.controlei.domain.models.entities.Investment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class InvestmentMapper {

    public Investment toEntity(CreateInvestmentRequest request, UUID familyId) {
        return new Investment(
                UUID.randomUUID(),
                familyId,
                request.userId(),
                request.categoryId(),
                request.name(),
                request.type(),
                request.currentAmount(),
                request.referenceDate(),
                request.notes(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null
        );
    }

    public InvestmentResponse toResponse(Investment investment) {
        return new InvestmentResponse(
                investment.getId(),
                investment.getFamilyId(),
                investment.getUserId(),
                investment.getCategoryId(),
                investment.getName(),
                investment.getType(),
                investment.getCurrentAmount(),
                investment.getReferenceDate(),
                investment.getNotes(),
                investment.getCreatedAt(),
                investment.getUpdatedAt()
        );
    }

    public void updateEntity(Investment investment, UpdateInvestmentRequest request) {
        investment.setCategoryId(request.categoryId());
        investment.setName(request.name());
        investment.setType(request.type());
        investment.setCurrentAmount(request.currentAmount());
        investment.setReferenceDate(request.referenceDate());
        investment.setNotes(request.notes());
        investment.setUpdatedAt(LocalDateTime.now());
    }
}
