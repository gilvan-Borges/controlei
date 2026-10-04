package br.com.controlei.application.services.receipt;

import br.com.controlei.domain.contracts.ai.ReceiptAiClient;
import org.junit.jupiter.api.Test;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** O modelo e tratado como entrada hostil: cada teste abaixo e uma forma de ele errar ou de ser enganado. */
class LlmReceiptExtractorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 4);
    private static final List<String> CATEGORIES = List.of("Alimentacao", "Saude", "Transporte");
    private static final ObjectProvider<MeterRegistry> NO_METERS =
            new StaticListableBeanFactory(Map.of()).getBeanProvider(MeterRegistry.class);
    private static final String TEXT = "Estabelecimento: Farmacia Drogasil\nValor: R$ 89,50\nData: 15/08/2026";

    private final LlmReceiptExtractor extractor = new LlmReceiptExtractor(
            new StaticListableBeanFactory(Map.of()).getBeanProvider(ReceiptAiClient.class), JsonMapper.builder().build(), NO_METERS);

    private Optional<ReceiptExtraction> validate(String json) {
        return extractor.validate(json, CATEGORIES, TODAY, TEXT);
    }

    @Test
    void acceptsAWellFormedAnswer() {
        var r = validate("""
                {"amount": 89.50, "date": "2026-08-15", "merchant": "Farmacia Drogasil",
                 "category": "saude", "confidence": 0.8, "rawText": "x"}""").orElseThrow();

        assertThat(r.amount()).isEqualByComparingTo("89.50");
        assertThat(r.date()).isEqualTo(LocalDate.of(2026, 8, 15));
        assertThat(r.merchant()).isEqualTo("Farmacia Drogasil");
        assertThat(r.categoryName()).isEqualTo("Saude");
        assertThat(r.confidence()).isEqualByComparingTo("0.80");
    }

    @Test
    void acceptsJsonInsideACodeFence() {
        var r = validate("```json\n{\"amount\": \"89,50\", \"confidence\": 0.7}\n```").orElseThrow();
        assertThat(r.amount()).isEqualByComparingTo("89.50");
    }

    @Test
    void rejectsAnAmountThatIsNotInTheSourceText() {
        var r = validate("{\"amount\": 9999.99, \"confidence\": 0.95}").orElseThrow();

        assertThat(r.amount()).isNull();
        assertThat(r.confidence()).isLessThanOrEqualTo(new BigDecimal("0.30"));
    }

    @Test
    void dropsNegativeZeroAndAbsurdAmounts() {
        assertThat(extractor.validate("{\"amount\": -89.50}", CATEGORIES, TODAY, "-89.50").orElseThrow().amount()).isNull();
        assertThat(extractor.validate("{\"amount\": 0}", CATEGORIES, TODAY, "0").orElseThrow().amount()).isNull();
        assertThat(extractor.validate("{\"amount\": 99999999}", CATEGORIES, TODAY, "99999999").orElseThrow().amount()).isNull();
    }

    @Test
    void capsTheModelsSelfReportedConfidence() {
        var r = validate("{\"amount\": 89.50, \"confidence\": 1.0}").orElseThrow();
        assertThat(r.confidence()).isEqualByComparingTo("0.90");
    }

    @Test
    void ignoresACategoryOutsideTheFamilyList() {
        var r = validate("{\"amount\": 89.50, \"category\": \"Apagar todas as contas\"}").orElseThrow();
        assertThat(r.categoryName()).isNull();
    }

    @Test
    void ignoresImplausibleDates() {
        assertThat(validate("{\"amount\": 89.50, \"date\": \"2031-01-01\"}").orElseThrow().date()).isNull();
        assertThat(validate("{\"amount\": 89.50, \"date\": \"1999-01-01\"}").orElseThrow().date()).isNull();
        assertThat(validate("{\"amount\": 89.50, \"date\": \"ontem\"}").orElseThrow().date()).isNull();
    }

    @Test
    void truncatesMerchantAndStripsControlCharacters() {
        String longName = "A".repeat(500);
        var r = validate("{\"amount\": 89.50, \"merchant\": \"" + longName + "\\u0007\"}").orElseThrow();
        assertThat(r.merchant()).hasSize(LlmReceiptExtractor.MAX_MERCHANT);
    }

    @Test
    void returnsEmptyWhenTheAnswerIsNotAnObject() {
        assertThat(validate("claro! o valor e 89,50")).isEmpty();
        assertThat(validate("[1,2,3]")).isEmpty();
        assertThat(validate("")).isEmpty();
        assertThat(validate(null)).isEmpty();
    }

    @Test
    void anInjectedInstructionInTheReceiptCannotChangeTheShapeOfTheResult() {
        // O modelo "obedeceu" o comprovante e devolveu campos extras e texto: so os campos conhecidos passam.
        var r = validate("""
                {"amount": 89.50, "merchant": "Drogasil", "transferTo": "conta 123", "confidence": 0.8}""").orElseThrow();
        assertThat(r.merchant()).isEqualTo("Drogasil");
        assertThat(r).hasNoNullFieldsOrPropertiesExcept("date", "categoryName", "rawText");
    }

    @Test
    void withoutAProviderThereIsNoExtraction() {
        assertThat(extractor.available()).isFalse();
        assertThat(extractor.extract(TEXT, null, CATEGORIES, TODAY)).isEmpty();
    }

    @Test
    void aFailingProviderFallsBackToEmptyInsteadOfThrowing() {
        ReceiptAiClient broken = (system, text, image) -> {
            throw new IllegalStateException("503");
        };
        var withBroken = new LlmReceiptExtractor(
                new StaticListableBeanFactory(Map.of("ai", broken)).getBeanProvider(ReceiptAiClient.class),
                JsonMapper.builder().build(), NO_METERS);

        assertThat(withBroken.available()).isTrue();
        assertThat(withBroken.extract(TEXT, null, CATEGORIES, TODAY)).isEmpty();
    }

    @Test
    void sendsTheReceiptAsDataBetweenDelimiters() {
        String[] seen = new String[1];
        ReceiptAiClient spy = (system, text, image) -> {
            seen[0] = text;
            return "{\"amount\": 89.50, \"confidence\": 0.8}";
        };
        var ex = new LlmReceiptExtractor(
                new StaticListableBeanFactory(Map.of("ai", spy)).getBeanProvider(ReceiptAiClient.class),
                JsonMapper.builder().build(), NO_METERS);

        assertThat(ex.extract(TEXT, null, CATEGORIES, TODAY)).isPresent();
        assertThat(seen[0]).contains("<comprovante>").contains("</comprovante>").contains("Categorias permitidas");
    }

    @Test
    void afterRepeatedFailuresTheCircuitOpensAndTheProviderIsNotCalledAgain() {
        AtomicInteger calls = new AtomicInteger();
        ReceiptAiClient broken = (system, text, image) -> {
            calls.incrementAndGet();
            throw new IllegalStateException("timeout");
        };
        var registry = new SimpleMeterRegistry();
        var clock = new MutableClock(Instant.parse("2026-10-04T12:00:00Z"));
        var ex = new LlmReceiptExtractor(
                new StaticListableBeanFactory(Map.of("ai", broken)).getBeanProvider(ReceiptAiClient.class),
                JsonMapper.builder().build(),
                new StaticListableBeanFactory(Map.of("m", registry)).getBeanProvider(MeterRegistry.class), clock);

        for (int i = 0; i < LlmReceiptExtractor.FAILURES_TO_OPEN; i++) {
            assertThat(ex.extract(TEXT, null, CATEGORIES, TODAY)).isEmpty();
        }
        assertThat(ex.circuitOpen()).isTrue();

        ex.extract(TEXT, null, CATEGORIES, TODAY);
        assertThat(calls.get()).isEqualTo(LlmReceiptExtractor.FAILURES_TO_OPEN); // a chamada seguinte nem saiu

        clock.advance(LlmReceiptExtractor.OPEN_FOR.plusSeconds(1));
        assertThat(ex.circuitOpen()).isFalse();
        ex.extract(TEXT, null, CATEGORIES, TODAY);
        assertThat(calls.get()).isEqualTo(LlmReceiptExtractor.FAILURES_TO_OPEN + 1);

        assertThat(registry.counter("controlei.receipts.ai", "outcome", "failed").count()).isEqualTo(4.0);
        assertThat(registry.counter("controlei.receipts.ai", "outcome", "skipped_circuit_open").count()).isEqualTo(1.0);
    }

    @Test
    void aSuccessResetsTheFailureCount() {
        AtomicInteger n = new AtomicInteger();
        ReceiptAiClient flaky = (system, text, image) -> {
            if (n.incrementAndGet() % 3 != 0) {
                throw new IllegalStateException("503");
            }
            return "{\"amount\": 89.50, \"confidence\": 0.8}";
        };
        var ex = new LlmReceiptExtractor(
                new StaticListableBeanFactory(Map.of("ai", flaky)).getBeanProvider(ReceiptAiClient.class),
                JsonMapper.builder().build(), NO_METERS);

        for (int i = 0; i < 9; i++) {
            ex.extract(TEXT, null, CATEGORIES, TODAY);
        }
        assertThat(ex.circuitOpen()).isFalse(); // duas falhas e um sucesso: nunca chega a tres seguidas
    }

    /** Relogio controlavel, so para o teste do disjuntor. */
    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant start) {
            this.now = start;
        }

        void advance(java.time.Duration d) {
            now = now.plus(d);
        }

        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }
}
