# TASK-01: Hardening de Segurança, JWT em Env, Refresh Token e Rate Limiting

## 🎯 Objetivo
Blindar a segurança da aplicação eliminando secrets hardcoded, implementando tokens de curta duração com Refresh Token rotativo, rate limiting contra ataques de força bruta, armazenamento seguro de credenciais em cookies HttpOnly e headers de segurança HTTP.

---

## 📋 Requisitos Técnicos

### 1. Externalização e Validação do Segredo JWT
- Remover qualquer valor default de `jwt.secret` no `application.properties`.
- Injetar `${JWT_SECRET}` obrigatoriamente.
- Criar validador no boot do Spring (`JwtSecretValidator`) para interromper a subida da aplicação se a chave for menor que 256 bits (32 caracteres) ou for nula em ambiente de produção.

### 2. Implementação de Refresh Token Rotativo
- Reduzir a validade do **Access Token (JWT)** para **15 minutos**.
- Criar a entidade e migration `refresh_tokens`:
  ```sql
  CREATE TABLE refresh_tokens (
      id UUID PRIMARY KEY,
      user_id UUID NOT NULL REFERENCES users(id),
      token VARCHAR(500) NOT NULL UNIQUE,
      expires_at TIMESTAMP NOT NULL,
      revoked BOOLEAN DEFAULT FALSE,
      created_at TIMESTAMP NOT NULL
  );
  CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
  ```
- Endpoints:
  - `POST /api/v1/auth/refresh`: Recebe o refresh token (preferencialmente via cookie `HttpOnly` ou body), valida, revoga o token anterior e emite um novo par (Access Token + novo Refresh Token).
  - `POST /api/v1/auth/logout`: Revoga o refresh token e limpa o cookie.

### 3. Rate Limiting no Nginx e Spring Boot
- No Nginx: limitar endpoints `/api/v1/auth/login` e `/api/v1/auth/register-family` a 5 requisições por minuto com burst de 3.
- No Backend: integrar interceptor com **Bucket4j** utilizando Redis para persistência de tokens por IP.

### 4. Headers de Segurança HTTP
- Configurar no `SecurityConfig.java` e no `nginx.conf`:
  - `Content-Security-Policy`
  - `X-Frame-Options: SAMEORIGIN`
  - `X-Content-Type-Options: nosniff`
  - `Referrer-Policy: strict-origin-when-cross-origin`

---

## 🧪 Critérios de Aceite e Testes
1. O backend **não** deve iniciar se `JWT_SECRET` não for fornecida no perfil `prod`.
2. O endpoint `/api/v1/auth/refresh` deve emitir novo JWT válido se o refresh token existir e não estiver revogado.
3. Se um refresh token for reutilizado após rotação, invalidar toda a cadeia de tokens daquele usuário (prevenção de token theft).
4. O Nginx deve retornar status `429 Too Many Requests` após exceder o limite de tentativas de login.
5. Testes unitários e de integração cobrindo o ciclo completo de login, refresh e logout.
