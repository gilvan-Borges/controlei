package br.com.controlei.application.exceptions.handler;

import br.com.controlei.application.exceptions.BusinessException;
import br.com.controlei.application.exceptions.ForbiddenException;
import br.com.controlei.application.exceptions.NotFoundException;
import br.com.controlei.application.exceptions.UnauthorizedException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
class TestExceptionController {

    @GetMapping("/not-found")
    public String triggerNotFound() {
        throw new NotFoundException("Recurso nao encontrado");
    }

    @GetMapping("/business-error")
    public String triggerBusinessError() {
        throw new BusinessException("Erro de negocio");
    }

    @GetMapping("/forbidden")
    public String triggerForbidden() {
        throw new ForbiddenException("Acesso negado");
    }

    @GetMapping("/unauthorized")
    public String triggerUnauthorized() {
        throw new UnauthorizedException("Nao autorizado");
    }

    @GetMapping("/internal-error")
    public String triggerInternalError() {
        throw new RuntimeException("Erro interno inesperado");
    }

    @PostMapping("/validation")
    public String triggerValidation(@Valid @RequestBody TestDto dto) {
        return "ok";
    }

    @GetMapping("/path-variable/{id}")
    public String triggerPathVariable(@PathVariable Long id) {
        return "ok";
    }

    @GetMapping("/validated-path/{id}")
    public String triggerValidatedPath(@PathVariable @Min(value = 1, message = "ID deve ser maior que zero") Long id) {
        return "ok";
    }

    @GetMapping("/validated-param")
    public String triggerValidatedParam(@RequestParam @NotBlank(message = "Nome e obrigatorio") String name) {
        return "ok";
    }

    static class TestDto {
        @NotBlank(message = "Nome e obrigatorio")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
