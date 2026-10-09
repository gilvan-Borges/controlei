package br.com.controlei.domain.models.dtos.auth;

/**
 * Configuracao publica da tela de login: se o cadastro de familias esta aberto e se existe a entrada de visitante
 * (modo demonstracao). Nao revela nada alem do que a propria tela mostraria.
 */
public record PublicAuthConfig(boolean registrationEnabled, boolean demoEnabled) {
}
