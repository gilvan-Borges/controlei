package br.com.controlei.domain.contracts.repositories;

import java.util.UUID;

/** Interruptor do assistente de IA por familia. Sem linha para a familia, esta desligado. */
public interface AssistantSettingsRepositoryPort {

    boolean isEnabled(UUID familyId);

    void setEnabled(UUID familyId, boolean enabled, UUID changedBy);
}
