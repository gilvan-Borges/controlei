package br.com.controlei.domain.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Reduz "quem deve para quem" ao menor numero razoavel de pagamentos. Regra de dominio pura.
 *
 * <p>Entrada: saldo liquido por membro (positivo = tem a receber, negativo = deve). A soma dos saldos de uma familia
 * e zero. Saida: transferencias que, aplicadas, zeram todos os saldos. O guloso (maior devedor paga ao maior credor) nao
 * garante o minimo absoluto (isso e NP-dificil), mas gera no maximo n-1 transferencias e e deterministico: a ordem nao
 * depende da iteracao de um mapa.
 */
public final class DebtSimplifier {

    public record Transfer(UUID from, UUID to, BigDecimal amount) {}

    private DebtSimplifier() {
    }

    public static List<Transfer> simplify(Map<UUID, BigDecimal> netBalances) {
        Comparator<Map.Entry<UUID, BigDecimal>> byAmountDescThenId = Comparator
                .<Map.Entry<UUID, BigDecimal>, BigDecimal>comparing(Map.Entry::getValue).reversed()
                .thenComparing(Map.Entry::getKey);

        List<Map.Entry<UUID, BigDecimal>> debtors = new ArrayList<>();
        List<Map.Entry<UUID, BigDecimal>> creditors = new ArrayList<>();
        for (Map.Entry<UUID, BigDecimal> e : netBalances.entrySet()) {
            int sign = e.getValue().signum();
            if (sign < 0) {
                debtors.add(Map.entry(e.getKey(), e.getValue().abs()));
            } else if (sign > 0) {
                creditors.add(Map.entry(e.getKey(), e.getValue()));
            }
        }
        debtors.sort(byAmountDescThenId);
        creditors.sort(byAmountDescThenId);

        BigDecimal[] debt = debtors.stream().map(Map.Entry::getValue).toArray(BigDecimal[]::new);
        BigDecimal[] credit = creditors.stream().map(Map.Entry::getValue).toArray(BigDecimal[]::new);

        List<Transfer> transfers = new ArrayList<>();
        int d = 0;
        int c = 0;
        while (d < debt.length && c < credit.length) {
            BigDecimal amount = debt[d].min(credit[c]);
            transfers.add(new Transfer(debtors.get(d).getKey(), creditors.get(c).getKey(), amount));
            debt[d] = debt[d].subtract(amount);
            credit[c] = credit[c].subtract(amount);
            if (debt[d].signum() == 0) {
                d++;
            }
            if (credit[c].signum() == 0) {
                c++;
            }
        }
        return transfers;
    }
}
