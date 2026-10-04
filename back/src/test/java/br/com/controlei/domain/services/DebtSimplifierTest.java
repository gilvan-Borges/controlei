package br.com.controlei.domain.services;

import br.com.controlei.domain.services.DebtSimplifier.Transfer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DebtSimplifierTest {

    private static final UUID ANA = new UUID(0, 1);
    private static final UUID BIA = new UUID(0, 2);
    private static final UUID CAIO = new UUID(0, 3);

    private static Map<UUID, BigDecimal> balances(Object... pairs) {
        Map<UUID, BigDecimal> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((UUID) pairs[i], new BigDecimal((String) pairs[i + 1]));
        }
        return map;
    }

    @Test
    void oneDebtorPaysTheCreditor() {
        List<Transfer> t = DebtSimplifier.simplify(balances(ANA, "50.00", BIA, "-50.00"));

        assertThat(t).containsExactly(new Transfer(BIA, ANA, new BigDecimal("50.00")));
    }

    @Test
    void aChainCollapsesIntoDirectPayments() {
        // Ana recebe 30, Bia deve 10, Caio deve 20: dois pagamentos, nenhum intermediario
        List<Transfer> t = DebtSimplifier.simplify(balances(ANA, "30.00", BIA, "-10.00", CAIO, "-20.00"));

        assertThat(t).hasSize(2);
        assertThat(t).extracting(Transfer::to).containsOnly(ANA);
    }

    @Test
    void settledBalancesProduceNoTransfers() {
        assertThat(DebtSimplifier.simplify(balances(ANA, "0.00", BIA, "0.00"))).isEmpty();
        assertThat(DebtSimplifier.simplify(Map.of())).isEmpty();
    }

    @Test
    void theResultIsDeterministicRegardlessOfMapIterationOrder() {
        Map<UUID, BigDecimal> forward = new java.util.LinkedHashMap<>();
        forward.put(ANA, new BigDecimal("30"));
        forward.put(BIA, new BigDecimal("-10"));
        forward.put(CAIO, new BigDecimal("-20"));
        Map<UUID, BigDecimal> backward = new java.util.LinkedHashMap<>();
        backward.put(CAIO, new BigDecimal("-20"));
        backward.put(BIA, new BigDecimal("-10"));
        backward.put(ANA, new BigDecimal("30"));

        assertThat(DebtSimplifier.simplify(forward)).isEqualTo(DebtSimplifier.simplify(backward));
    }

    @Test
    void applyingTheTransfersZeroesEveryBalanceAndUsesAtMostNMinusOne() {
        // Propriedade: para saldos aleatorios que somam zero, as transferencias quitam todo mundo
        Random random = new Random(2026);
        for (int round = 0; round < 500; round++) {
            int n = 2 + random.nextInt(7);
            Map<UUID, BigDecimal> net = new HashMap<>();
            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < n - 1; i++) {
                BigDecimal v = BigDecimal.valueOf(random.nextInt(20_001) - 10_000, 2);
                net.put(new UUID(1, i), v);
                total = total.add(v);
            }
            net.put(new UUID(1, n - 1), total.negate()); // fecha a soma em zero

            Map<UUID, BigDecimal> remaining = new HashMap<>(net);
            List<Transfer> transfers = DebtSimplifier.simplify(net);
            for (Transfer t : transfers) {
                assertThat(t.amount()).isPositive();
                remaining.merge(t.from(), t.amount(), BigDecimal::add);          // quem paga reduz a divida
                remaining.merge(t.to(), t.amount().negate(), BigDecimal::add);   // quem recebe reduz o credito
            }

            assertThat(remaining.values()).allSatisfy(v -> assertThat(v).isEqualByComparingTo("0"));
            assertThat(transfers.size()).isLessThanOrEqualTo(n - 1);
        }
    }
}
