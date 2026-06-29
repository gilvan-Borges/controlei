package br.com.controlei.application.services;

import br.com.controlei.application.exceptions.NotFoundException;
import br.com.controlei.application.mappers.FamilyMapper;
import br.com.controlei.domain.contracts.repositories.FamilyRepositoryPort;
import br.com.controlei.domain.models.dtos.family.FamilyResponse;
import br.com.controlei.domain.models.entities.Family;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FamilyService {

    private final FamilyRepositoryPort familyRepository;
    private final FamilyMapper familyMapper;
    private final AuthorizationService authorizationService;

    public FamilyService(FamilyRepositoryPort familyRepository,
                         FamilyMapper familyMapper,
                         AuthorizationService authorizationService) {
        this.familyRepository = familyRepository;
        this.familyMapper = familyMapper;
        this.authorizationService = authorizationService;
    }

    public FamilyResponse getCurrentFamily() {
        UUID familyId = authorizationService.currentFamilyId();
        Family family = familyRepository.findByIdAndDeletedAtIsNull(familyId)
                .orElseThrow(() -> new NotFoundException("Familia nao encontrada"));

        return familyMapper.toResponse(family);
    }
}
