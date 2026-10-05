package br.com.controlei.application.contracts;

import java.util.UUID;

/**
 * Apaga de vez (sem soft delete) tudo o que uma familia lancou, preservando a familia, os usuarios e a assinatura.
 * Existe para o modo demonstracao: a familia de visitantes volta ao estado inicial todo dia.
 */
public interface FamilyDataCleaner {

    void wipe(UUID familyId);
}
