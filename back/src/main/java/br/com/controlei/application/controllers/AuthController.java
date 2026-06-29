package br.com.controlei.application.controllers;

import br.com.controlei.application.services.AuthService;
import br.com.controlei.domain.models.dtos.auth.LoginRequest;
import br.com.controlei.domain.models.dtos.auth.LoginResponse;
import br.com.controlei.domain.models.dtos.auth.RegisterFamilyRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register-family")
    public ResponseEntity<LoginResponse> registerFamily(@Valid @RequestBody RegisterFamilyRequest request) {
        return ResponseEntity.ok(authService.registerFamily(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
