package br.com.controlei.application.services.receipt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cota diaria de leituras por IA, por familia. Cada leitura custa dinheiro e uma chamada externa, e sem teto uma
 * unica conta poderia gastar o orcamento inteiro. Passou da cota, a leitura segue pelo plano B (regras ou "revisar").
 *
 * <p>Em memoria e por instancia: basta para uma instancia unica e nao exige nenhum servico extra. O mapa so guarda o
 * dia corrente, entao nao cresce alem do numero de familias ativas em um dia.
 */
@Component
public class AiQuota {

    private record Day(LocalDate date, int used) {}

    private final ConcurrentHashMap<UUID, Day> usage = new ConcurrentHashMap<>();
    private final int dailyLimit;
    private final Clock clock;

    @Autowired
    public AiQuota(@Value("${controlei.ai.daily-limit-per-family:30}") int dailyLimit) {
        this(dailyLimit, Clock.systemDefaultZone());
    }

    AiQuota(int dailyLimit, Clock clock) {
        this.dailyLimit = dailyLimit;
        this.clock = clock;
    }

    /** Reserva uma leitura. Falso quando a familia ja usou a cota de hoje. */
    public boolean tryAcquire(UUID familyId) {
        LocalDate today = LocalDate.now(clock);
        boolean[] granted = {false};
        usage.compute(familyId, (id, current) -> {
            int used = current != null && current.date().equals(today) ? current.used() : 0;
            if (used >= dailyLimit) {
                return new Day(today, used);
            }
            granted[0] = true;
            return new Day(today, used + 1);
        });
        if (usage.size() > 10_000) {
            usage.entrySet().removeIf(e -> !e.getValue().date().equals(today));
        }
        return granted[0];
    }
}
