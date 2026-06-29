package br.com.controlei.application.controllers;

import br.com.controlei.application.services.UserService;
import br.com.controlei.domain.models.dtos.user.CurrentUserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final UserService userService;

    public MeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<CurrentUserResponse> me() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }
}
