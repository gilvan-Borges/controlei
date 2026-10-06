package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.application.contracts.VoiceSettingsRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

/** JDBC simples, como o interruptor do assistente: uma linha por familia nao justifica entidade JPA e mapper. */
@Repository
public class VoiceSettingsJdbcAdapter implements VoiceSettingsRepository {

    private final JdbcTemplate jdbc;

    public VoiceSettingsJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean isEnabled(UUID familyId) {
        return jdbc.query("SELECT enabled FROM voice_settings WHERE family_id = ?",
                rs -> rs.next() && rs.getBoolean(1), familyId);
    }

    @Override
    public void setEnabled(UUID familyId, boolean enabled, UUID changedBy) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        int updated = jdbc.update("UPDATE voice_settings SET enabled = ?, changed_by = ?, changed_at = ? WHERE family_id = ?",
                enabled, changedBy, now, familyId);
        if (updated == 0) {
            jdbc.update("INSERT INTO voice_settings (family_id, enabled, changed_by, changed_at) VALUES (?, ?, ?, ?)",
                    familyId, enabled, changedBy, now);
        }
    }
}
