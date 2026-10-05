package br.com.controlei.infrastructure.configurations.demo;

import br.com.controlei.application.exceptions.ForbiddenException;
import br.com.controlei.application.services.DemoService;
import br.com.controlei.domain.contracts.AuthenticatedUserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * O visitante usa o app inteiro, menos o que tiraria a demonstracao dos proximos visitantes ou sairia do servidor:
 * mexer em usuarios (senha, e-mail, convites), assinatura, interruptor da IA, conexoes bancarias e envio de arquivos.
 * A excecao vira o 403 padrao da API pelo GlobalExceptionHandler, com a mensagem que a tela mostra.
 */
public class DemoGuardInterceptor implements HandlerInterceptor {

    private record Rule(String method, String pathPrefix) {
        boolean matches(String m, String path) {
            return (method == null || method.equalsIgnoreCase(m)) && path.startsWith(pathPrefix);
        }
    }

    /** method null = qualquer metodo que altera dados (GET continua liberado). */
    private static final List<Rule> BLOCKED = List.of(
            new Rule(null, "/api/v1/users"),
            new Rule(null, "/api/v1/families"),
            new Rule(null, "/api/v1/subscriptions"),
            new Rule(null, "/api/v1/bank-connections"),
            new Rule("PUT", "/api/v1/assistant/settings"),
            new Rule("POST", "/api/v1/receipts/scan"));

    private final DemoService demo;
    private final AuthenticatedUserContext currentUser;

    public DemoGuardInterceptor(DemoService demo, AuthenticatedUserContext currentUser) {
        this.demo = demo;
        this.currentUser = currentUser;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        boolean visitor = currentUser.getCurrentUser().map(u -> demo.isDemoFamily(u.familyId())).orElse(false);
        if (visitor && BLOCKED.stream().anyMatch(r -> r.matches(method, request.getRequestURI()))) {
            throw new ForbiddenException("Indisponível na demonstração. Crie sua própria instância para usar este recurso.");
        }
        return true;
    }
}
