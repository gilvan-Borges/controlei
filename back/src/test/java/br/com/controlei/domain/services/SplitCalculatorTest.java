package br.com.controlei.domain.services;

import br.com.controlei.domain.exceptions.DomainRuleException;
import br.com.controlei.domain.models.enums.SplitType;
import br.com.controlei.domain.services.SplitCalculator.Input;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Regra de dominio pura: sem Spring, sem banco, roda em milissegundos. */
class SplitCalculatorTest {

    private static List<Input> people(int n) {
        List<Input> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(new Input(UUID.randomUUID(), null));
        }
        return list;
    }

    private static BigDecimal sum(Map<UUID, BigDecimal> parts) {
        return parts.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Test
    void equalSplitGivesTheRoundingLeftoverToTheFirstMember() {
        List<Input> in = people(3);
        var parts = SplitCalculator.calculate(SplitType.EQUAL, new BigDecimal("100.00"), in);

        assertThat(parts.get(in.get(0).userId())).isEqualByComparingTo("33.34");
        assertThat(parts.get(in.get(1).userId())).isEqualByComparingTo("33.33");
        assertThat(parts.get(in.get(2).userId())).isEqualByComparingTo("33.33");
        assertThat(sum(parts)).isEqualByComparingTo("100.00");
    }

    @Test
    void thePartsAlwaysAddUpToTheTotalToTheCent() {
        // Propriedade: para qualquer total e numero de pessoas, nao se perde nem se cria dinheiro
        Random random = new Random(42);
        for (int i = 0; i < 2000; i++) {
            BigDecimal total = BigDecimal.valueOf(1 + random.nextInt(2_000_000), 2);
            int n = 1 + random.nextInt(9);
            assertThat(sum(SplitCalculator.calculate(SplitType.EQUAL, total, people(n))))
                    .as("igual: %s entre %s", total, n).isEqualByComparingTo(total);
        }
    }

    @Test
    void percentageSplitAbsorbsTheRoundingInTheLastMember() {
        Random random = new Random(7);
        for (int i = 0; i < 1000; i++) {
            BigDecimal total = BigDecimal.valueOf(1 + random.nextInt(1_000_000), 2);
            int a = 1 + random.nextInt(98);
            int b = 1 + random.nextInt(99 - a);
            List<Input> in = List.of(
                    new Input(UUID.randomUUID(), BigDecimal.valueOf(a)),
                    new Input(UUID.randomUUID(), BigDecimal.valueOf(b)),
                    new Input(UUID.randomUUID(), BigDecimal.valueOf(100 - a - b)));
            var parts = SplitCalculator.calculate(SplitType.PERCENTAGE, total, in);
            assertThat(sum(parts)).as("percentual: %s em %s/%s", total, a, b).isEqualByComparingTo(total);
        }
    }

    @Test
    void exactAmountsMustAddUpToTheTotal() {
        List<Input> ok = List.of(new Input(UUID.randomUUID(), new BigDecimal("60.00")), new Input(UUID.randomUUID(), new BigDecimal("40.00")));
        assertThat(sum(SplitCalculator.calculate(SplitType.EXACT_AMOUNT, new BigDecimal("100.00"), ok))).isEqualByComparingTo("100.00");

        assertThatThrownBy(() -> SplitCalculator.calculate(SplitType.EXACT_AMOUNT, new BigDecimal("100.01"), ok))
                .isInstanceOf(DomainRuleException.class).hasMessageContaining("soma das partes");
    }

    @Test
    void percentagesMustAddUpTo100() {
        List<Input> bad = List.of(new Input(UUID.randomUUID(), BigDecimal.valueOf(60)), new Input(UUID.randomUUID(), BigDecimal.valueOf(30)));
        assertThatThrownBy(() -> SplitCalculator.calculate(SplitType.PERCENTAGE, BigDecimal.TEN, bad))
                .isInstanceOf(DomainRuleException.class).hasMessageContaining("100%");
    }

    @Test
    void theSameMemberTwiceIsRefusedInsteadOfSilentlyLosingMoney() {
        UUID same = UUID.randomUUID();
        List<Input> dup = List.of(new Input(same, new BigDecimal("50.00")), new Input(same, new BigDecimal("50.00")));

        assertThatThrownBy(() -> SplitCalculator.calculate(SplitType.EXACT_AMOUNT, new BigDecimal("100.00"), dup))
                .isInstanceOf(DomainRuleException.class).hasMessageContaining("duas vezes");
    }

    @Test
    void negativeZeroAndMissingValuesAreRefused() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        assertThatThrownBy(() -> SplitCalculator.calculate(SplitType.EXACT_AMOUNT, BigDecimal.TEN,
                List.of(new Input(a, new BigDecimal("-5")), new Input(b, new BigDecimal("15")))))
                .isInstanceOf(DomainRuleException.class);
        assertThatThrownBy(() -> SplitCalculator.calculate(SplitType.EXACT_AMOUNT, BigDecimal.TEN,
                List.of(new Input(a, null), new Input(b, BigDecimal.TEN))))
                .isInstanceOf(DomainRuleException.class);
        assertThatThrownBy(() -> SplitCalculator.calculate(SplitType.EQUAL, BigDecimal.ZERO, people(2)))
                .isInstanceOf(DomainRuleException.class);
        assertThatThrownBy(() -> SplitCalculator.calculate(SplitType.EQUAL, BigDecimal.TEN, List.of()))
                .isInstanceOf(DomainRuleException.class);
    }

    @Test
    void membersKeepTheOrderTheyWereGiven() {
        List<Input> in = people(5);
        var parts = SplitCalculator.calculate(SplitType.EQUAL, new BigDecimal("10.00"), in);
        assertThat(parts.keySet()).containsExactlyElementsOf(in.stream().map(Input::userId).toList());
    }
}
