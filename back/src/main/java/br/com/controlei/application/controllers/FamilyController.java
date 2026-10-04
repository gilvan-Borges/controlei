package br.com.controlei.application.controllers;

import br.com.controlei.application.services.FamilyService;
import br.com.controlei.domain.models.dtos.family.FamilyResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/families")
public class FamilyController {

    private final FamilyService familyService;

    public FamilyController(FamilyService familyService) {
        this.familyService = familyService;
    }

    @GetMapping("/current")
    public ResponseEntity<FamilyResponse> getCurrentFamily() {
        return ResponseEntity.ok(familyService.getCurrentFamily());
    }
}
