package br.com.controlei.infrastructure.configurations.demo;

import br.com.controlei.application.services.DemoService;
import br.com.controlei.domain.contracts.AuthenticatedUserContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** So existe com o modo demonstracao ligado: fora dele, nenhum custo por request. */
@Configuration
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoWebConfig implements WebMvcConfigurer {

    private final DemoService demo;
    private final AuthenticatedUserContext currentUser;

    public DemoWebConfig(DemoService demo, AuthenticatedUserContext currentUser) {
        this.demo = demo;
        this.currentUser = currentUser;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new DemoGuardInterceptor(demo, currentUser)).addPathPatterns("/api/**");
    }
}
