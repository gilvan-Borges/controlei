package br.com.controlei.application.services.receipt;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AiQuotaTest {

    private static final UUID FAMILY_A = UUID.randomUUID();
    private static final UUID FAMILY_B = UUID.randomUUID();

    private static Clock at(String instant) {
        return Clock.fixed(Instant.parse(instant), ZoneOffset.UTC);
    }

    @Test
    void grantsUpToTheDailyLimitThenRefuses() {
        var quota = new AiQuota(2, at("2026-10-04T12:00:00Z"));

        assertThat(quota.tryAcquire(FAMILY_A)).isTrue();
        assertThat(quota.tryAcquire(FAMILY_A)).isTrue();
        assertThat(quota.tryAcquire(FAMILY_A)).isFalse();
    }

    @Test
    void eachFamilyHasItsOwnQuota() {
        var quota = new AiQuota(1, at("2026-10-04T12:00:00Z"));

        assertThat(quota.tryAcquire(FAMILY_A)).isTrue();
        assertThat(quota.tryAcquire(FAMILY_A)).isFalse();
        assertThat(quota.tryAcquire(FAMILY_B)).isTrue();
    }

    @Test
    void theQuotaResetsOnTheNextDay() {
        var today = new AiQuota(1, at("2026-10-04T12:00:00Z"));
        assertThat(today.tryAcquire(FAMILY_A)).isTrue();
        assertThat(today.tryAcquire(FAMILY_A)).isFalse();

        var tomorrow = new AiQuota(1, at("2026-10-05T12:00:00Z"));
        assertThat(tomorrow.tryAcquire(FAMILY_A)).isTrue();
    }

    @Test
    void aZeroLimitTurnsAiOffForEveryone() {
        assertThat(new AiQuota(0, at("2026-10-04T12:00:00Z")).tryAcquire(FAMILY_A)).isFalse();
    }
}
