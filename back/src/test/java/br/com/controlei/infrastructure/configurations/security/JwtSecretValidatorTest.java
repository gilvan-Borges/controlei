package br.com.controlei.infrastructure.configurations.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtSecretValidatorTest {

    private static final String STRONG = "q8Zr3vN1xT6mB9cL2pW5yH7kD4fG0sJa";

    @Test
    void acceptsAStrongSecretInProduction() {
        assertThatCode(() -> JwtSecretValidator.check(STRONG, true)).doesNotThrowAnyException();
    }

    @Test
    void rejectsEmptyAndShortSecretsEverywhere() {
        assertThatThrownBy(() -> JwtSecretValidator.check("", false)).hasMessageContaining("obrigatoria");
        assertThatThrownBy(() -> JwtSecretValidator.check(null, false)).hasMessageContaining("obrigatoria");
        assertThatThrownBy(() -> JwtSecretValidator.check("curta", false)).hasMessageContaining("32");
    }

    @Test
    void productionRejectsTheExampleValuesThatShipWithTheRepository() {
        assertThatThrownBy(() -> JwtSecretValidator.check("troque-por-um-segredo-aleatorio-de-pelo-menos-32-caracteres", true))
                .hasMessageContaining("exemplo");
        assertThatThrownBy(() -> JwtSecretValidator.check("dev-only-secret-not-for-production-0123456789", true))
                .hasMessageContaining("exemplo");
    }

    @Test
    void productionRejectsLowVarietySecrets() {
        assertThatThrownBy(() -> JwtSecretValidator.check("a".repeat(40), true)).hasMessageContaining("variedade");
        assertThatThrownBy(() -> JwtSecretValidator.check("abababababababababababababababab", true))
                .hasMessageContaining("variedade");
    }

    @Test
    void localDevelopmentMayUseTheDevDefault() {
        assertThatCode(() -> JwtSecretValidator.check("dev-only-secret-not-for-production-0123456789", false))
                .doesNotThrowAnyException();
    }
}
