package br.com.controlei.infrastructure.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class RedisIdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(RedisIdempotencyService.class);

    private final StringRedisTemplate redisTemplate;
    private static final String IDEMPOTENCY_PREFIX = "idempotency:event:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    public RedisIdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Tenta registrar o processamento do evento.
     * Retorna true se é a primeira vez que o evento está sendo processado (sucesso).
     * Retorna false se o evento já foi processado anteriormente (duplicata evitada).
     */
    /** Desfaz a reserva de um evento cujo processamento falhou, para a reentrega poder tentar de novo. */
    public void release(UUID eventId) {
        if (eventId != null) {
            redisTemplate.delete(IDEMPOTENCY_PREFIX + eventId);
        }
    }

    public boolean acquireIdempotency(UUID eventId) {
        if (eventId == null) {
            return true;
        }

        String key = IDEMPOTENCY_PREFIX + eventId;
        Boolean isFirstTime = redisTemplate.opsForValue()
                .setIfAbsent(key, "PROCESSED", DEFAULT_TTL);

        boolean acquired = Boolean.TRUE.equals(isFirstTime);
        if (!acquired) {
            log.warn("Evento duplicado detectado e descartado com sucesso via Redis: [{}]", eventId);
        }
        return acquired;
    }
}
