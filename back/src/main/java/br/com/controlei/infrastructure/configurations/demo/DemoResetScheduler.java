package br.com.controlei.infrastructure.configurations.demo;

import br.com.controlei.application.services.DemoService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Prepara a familia de demonstracao na subida (cada deploy comeca limpo) e a recria todo dia, de madrugada no horario
 * de Brasilia, para nenhum visitante encontrar o que o anterior deixou.
 */
@Component
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoResetScheduler implements ApplicationRunner {

    private final DemoService demo;

    public DemoResetScheduler(DemoService demo) {
        this.demo = demo;
    }

    @Override
    public void run(ApplicationArguments args) {
        demo.reset();
    }

    @Scheduled(cron = "${app.demo.reset-cron:0 0 4 * * *}", zone = "America/Sao_Paulo")
    public void dailyReset() {
        demo.reset();
    }
}
