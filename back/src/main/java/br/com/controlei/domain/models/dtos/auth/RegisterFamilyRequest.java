package br.com.controlei.domain.models.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterFamilyRequest(
        @NotBlank(message = "Nome da familia e obrigatorio")
        @Size(min = 2, max = 255, message = "Nome da familia deve ter entre 2 e 255 caracteres")
        String familyName,

        @NotBlank(message = "Nome do responsavel e obrigatorio")
        @Size(min = 2, max = 255, message = "Nome do responsavel deve ter entre 2 e 255 caracteres")
        String responsibleName,

        @NotBlank(message = "Email e obrigatorio")
        @Email(message = "Email invalido")
        String email,

        @NotBlank(message = "Senha e obrigatoria")
        @Size(min = 6, message = "Senha deve ter no minimo 6 caracteres")
        String password
) {
}
