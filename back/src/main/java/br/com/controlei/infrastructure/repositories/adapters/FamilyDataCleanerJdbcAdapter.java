package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.application.contracts.FamilyDataCleaner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * DELETE por family_id, das tabelas filhas para as maes (as chaves estrangeiras nao tem ON DELETE CASCADE).
 * Uma tabela nova com family_id precisa entrar aqui: o FamilyDataCleanerIntegrationTest falha se ela ficar de fora.
 */
@Repository
public class FamilyDataCleanerJdbcAdapter implements FamilyDataCleaner {

    /** Ordem importa: cada tabela vem antes das que ela referencia. */
    public static final List<String> TABLES = List.of(
            "expense_split_shares",
            "split_settlements",
            "expense_splits",
            "goal_contributions",
            "investment_transactions",
            "bank_sync_mappings",
            "receipt_scans",
            "attachments",
            "credit_card_transactions",
            "invoices",
            "installments",
            "transactions",
            "recurring_transactions",
            "budgets",
            "debts",
            "investments",
            "credit_cards",
            "financial_goals",
            "bank_connections",
            "notifications",
            "audit_logs",
            "assistant_settings",
            "voice_settings",
            "accounts",
            "categories");

    /** Tabelas com family_id que o reset preserva de proposito. */
    public static final List<String> KEPT = List.of("users", "subscriptions");

    private final JdbcTemplate jdbc;

    public FamilyDataCleanerJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void wipe(UUID familyId) {
        for (String table : TABLES) {
            // Nome da tabela vem da lista fixa acima, nunca de entrada externa.
            jdbc.update("DELETE FROM " + table + " WHERE family_id = ?", familyId);
        }
    }
}
