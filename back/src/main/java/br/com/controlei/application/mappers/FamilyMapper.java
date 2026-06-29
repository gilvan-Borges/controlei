package br.com.controlei.application.mappers;

import br.com.controlei.domain.models.dtos.family.FamilyResponse;
import br.com.controlei.domain.models.entities.Family;
import org.springframework.stereotype.Component;

@Component
public class FamilyMapper {

    public FamilyResponse toResponse(Family family) {
        return new FamilyResponse(
                family.getId(),
                family.getName(),
                family.getResponsibleUserId(),
                family.getCreatedAt(),
                family.getUpdatedAt()
        );
    }
}
