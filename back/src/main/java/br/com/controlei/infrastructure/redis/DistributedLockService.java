package br.com.controlei.infrastructure.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class DistributedLockService {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockService.class);

    private final StringRedisTemplate redisTemplate;
    private static final String LOCK_PREFIX = "lock:";
    private static final Duration DEFAULT_LOCK_TIMEOUT = Duration.ofSeconds(10);

    public DistributedLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Executa uma operação protegida por lock distribuído no Redis.
     * Impede que duas transações ou liquidações concorrentes alterem a mesma conta/recurso ao mesmo tempo.
     */
    public <T> T executeWithLock(String resourceType, UUID resourceId, Supplier<T> action) {
        String lockKey = LOCK_PREFIX + resourceType + ":" + resourceId;
        String lockValue = UUID.randomUUID().toString();

        boolean locked = false;
        try {
            locked = Boolean.TRUE.equals(
                    redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, DEFAULT_LOCK_TIMEOUT)
            );

            if (!locked) {
                // Tenta mais uma vez após pequeno backoff
                Thread.sleep(100);
                locked = Boolean.TRUE.equals(
                        redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, DEFAULT_LOCK_TIMEOUT)
                );
            }

            if (!locked) {
                throw new IllegalStateException("Recurso ocupado no momento (" + resourceType + ": " + resourceId + "). Tente novamente.");
            }

            return action.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Operação interrompida enquanto aguardava lock distribuído", e);
        } finally {
            if (locked) {
                try {
                    String currentValue = redisTemplate.opsForValue().get(lockKey);
                    if (lockValue.equals(currentValue)) {
                        redisTemplate.delete(lockKey);
                    }
                } catch (Exception ex) {
                    log.error("Erro ao liberar lock [{}]: {}", lockKey, ex.getMessage());
                }
            }
        }
    }
}
