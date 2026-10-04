package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.AssistantSettingsRepositoryPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

/** JDBC simples: uma tabela de uma linha por familia nao justifica entidade JPA e mapper. */
@Repository
public class AssistantSettingsJdbcAdapter implements AssistantSettingsRepositoryPort {

    private final JdbcTemplate jdbc;

    public AssistantSettingsJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean isEnabled(UUID familyId) {
        return jdbc.query("SELECT enabled FROM assistant_settings WHERE family_id = ?",
                rs -> rs.next() && rs.getBoolean(1), familyId);
    }

    @Override
    public void setEnabled(UUID familyId, boolean enabled, UUID changedBy) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        int updated = jdbc.update("UPDATE assistant_settings SET enabled = ?, changed_by = ?, changed_at = ? WHERE family_id = ?",
                enabled, changedBy, now, familyId);
        if (updated == 0) {
            jdbc.update("INSERT INTO assistant_settings (family_id, enabled, changed_by, changed_at) VALUES (?, ?, ?, ?)",
                    familyId, enabled, changedBy, now);
        }
    }
}
