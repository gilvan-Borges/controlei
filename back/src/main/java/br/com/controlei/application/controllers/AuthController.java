package br.com.controlei.application.controllers;

import br.com.controlei.application.services.AuthService;
import br.com.controlei.domain.models.dtos.auth.LoginRequest;
import br.com.controlei.domain.models.dtos.auth.LoginResponse;
import br.com.controlei.domain.models.dtos.auth.PublicAuthConfig;
import br.com.controlei.domain.models.dtos.auth.RefreshRequest;
import br.com.controlei.domain.models.dtos.auth.RegisterFamilyRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/config")
    public ResponseEntity<PublicAuthConfig> config() {
        return ResponseEntity.ok(authService.publicConfig());
    }

    /** Entrada de visitante do modo demonstracao (404 quando o modo esta desligado). */
    @PostMapping("/demo")
    public ResponseEntity<LoginResponse> demo() {
        return ResponseEntity.ok(authService.loginAsDemoVisitor());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) RefreshRequest request) {
        if (request != null && request.refreshToken() != null) {
            authService.logout(request.refreshToken());
        }
        return ResponseEntity.noContent().build();
    }
}
