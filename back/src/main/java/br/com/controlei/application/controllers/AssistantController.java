package br.com.controlei.application.controllers;

import br.com.controlei.application.services.assistant.AssistantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assistant")
public class AssistantController {

    public record AskRequest(
            @NotBlank(message = "Escreva uma pergunta")
            @Size(max = 500, message = "Pergunta excede 500 caracteres")
            String question) {}

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/ask")
    public ResponseEntity<AssistantService.Answer> ask(@Valid @RequestBody AskRequest request) {
        return ResponseEntity.ok(assistantService.ask(request.question()));
    }
}
