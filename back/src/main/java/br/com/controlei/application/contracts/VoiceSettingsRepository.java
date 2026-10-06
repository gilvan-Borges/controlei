package br.com.controlei.application.contracts;

import java.util.UUID;

/**
 * Interruptor da voz do assistente por familia. Separado do interruptor do assistente: ligar a voz manda o AUDIO da
 * pessoa para um provedor externo, e isso pede um aceite proprio. Sem linha para a familia, esta desligado.
 */
public interface VoiceSettingsRepository {

    boolean isEnabled(UUID familyId);

    void setEnabled(UUID familyId, boolean enabled, UUID changedBy);
}
