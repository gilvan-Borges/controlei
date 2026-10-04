package br.com.controlei.domain.services;

import br.com.controlei.domain.exceptions.DomainRuleException;
import br.com.controlei.domain.models.enums.SplitType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Divide uma despesa entre membros. Regra de dominio pura (sem Spring, sem banco), por isso testavel por propriedade.
 *
 * <p><b>Invariante:</b> a soma das partes devolvidas e EXATAMENTE o total, em centavos. Nenhuma divisao perde ou cria
 * dinheiro: a sobra do arredondamento vai para uma parte so (a primeira na divisao igual, a ultima na percentual).
 */
public final class SplitCalculator {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    /** @param amountOrPercentage valor ou percentual, conforme o tipo; ignorado na divisao igual */
    public record Input(UUID userId, BigDecimal amountOrPercentage) {}

    private SplitCalculator() {
    }

    /** @return usuario -> parte, na ordem em que os membros foram informados */
    public static Map<UUID, BigDecimal> calculate(SplitType type, BigDecimal total, List<Input> inputs) {
        if (total == null || total.signum() <= 0) {
            throw new DomainRuleException("O total da despesa deve ser maior que zero");
        }
        if (inputs == null || inputs.isEmpty()) {
            throw new DomainRuleException("Informe ao menos um membro na divisao");
        }
        Set<UUID> seen = new HashSet<>();
        for (Input in : inputs) {
            // Sem esta checagem, o mesmo membro duas vezes sobrescreveria a propria parte e o dinheiro sumiria
            if (!seen.add(in.userId())) {
                throw new DomainRuleException("O mesmo membro nao pode aparecer duas vezes na divisao");
            }
        }

        return switch (type) {
            case EQUAL -> equal(total, inputs);
            case EXACT_AMOUNT -> exact(total, inputs);
            case PERCENTAGE -> percentage(total, inputs);
        };
    }

    private static Map<UUID, BigDecimal> equal(BigDecimal total, List<Input> inputs) {
        int n = inputs.size();
        BigDecimal share = total.divide(BigDecimal.valueOf(n), 2, RoundingMode.DOWN);
        BigDecimal remainder = total.subtract(share.multiply(BigDecimal.valueOf(n)));

        Map<UUID, BigDecimal> result = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            result.put(inputs.get(i).userId(), i == 0 ? share.add(remainder) : share);
        }
        return result;
    }

    private static Map<UUID, BigDecimal> exact(BigDecimal total, List<Input> inputs) {
        Map<UUID, BigDecimal> result = new LinkedHashMap<>();
        BigDecimal sum = BigDecimal.ZERO;
        for (Input in : inputs) {
            BigDecimal amount = in.amountOrPercentage();
            if (amount == null) {
                throw new DomainRuleException("Valor individual obrigatorio na divisao por valor exato");
            }
            if (amount.signum() <= 0) {
                throw new DomainRuleException("Cada parte deve ser maior que zero");
            }
            result.put(in.userId(), amount);
            sum = sum.add(amount);
        }
        if (sum.compareTo(total) != 0) {
            throw new DomainRuleException("A soma das partes (" + sum + ") deve ser igual ao total da despesa (" + total + ")");
        }
        return result;
    }

    private static Map<UUID, BigDecimal> percentage(BigDecimal total, List<Input> inputs) {
        BigDecimal percentSum = BigDecimal.ZERO;
        for (Input in : inputs) {
            BigDecimal pct = in.amountOrPercentage();
            if (pct == null) {
                throw new DomainRuleException("Percentual obrigatorio na divisao por porcentagem");
            }
            if (pct.signum() <= 0) {
                throw new DomainRuleException("Cada percentual deve ser maior que zero");
            }
            percentSum = percentSum.add(pct);
        }
        if (percentSum.compareTo(HUNDRED) != 0) {
            throw new DomainRuleException("A soma das porcentagens deve ser exatamente 100%");
        }

        Map<UUID, BigDecimal> result = new LinkedHashMap<>();
        BigDecimal running = BigDecimal.ZERO;
        int last = inputs.size() - 1;
        for (int i = 0; i < inputs.size(); i++) {
            Input in = inputs.get(i);
            if (i == last) {
                result.put(in.userId(), total.subtract(running)); // a ultima absorve o arredondamento
            } else {
                BigDecimal part = total.multiply(in.amountOrPercentage()).divide(HUNDRED, 2, RoundingMode.HALF_EVEN);
                result.put(in.userId(), part);
                running = running.add(part);
            }
        }
        return result;
    }
}
