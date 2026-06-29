# Ambiente Local - Controlei

## Pré-requisitos

- Java 21+
- Docker e Docker Compose
- Maven (ou use o wrapper `mvnw.cmd`)

## Iniciar o Banco de Dados

```powershell
docker compose up -d
```

O PostgreSQL estará disponível em `localhost:5432`.

## Configurar Variáveis de Ambiente

Copie o arquivo de exemplo:

```powershell
Copy-Item .env.example .env
```

Edite `.env` com suas configurações:

```properties
DB_HOST=localhost
DB_PORT=5432
DB_NAME=controlei
DB_USER=controlei
DB_PASSWORD=controlei123
JWT_SECRET=sua-chave-secreta-aqui-minimo-32-caracteres
JWT_EXPIRATION_HOURS=24
```

## Iniciar o Backend

```powershell
cd back
.\mvnw.cmd spring-boot:run
```

O backend estará disponível em `http://localhost:8080`.

## Health Check

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/health" -Method Get
```

Ou usando curl (se instalado):

```powershell
curl http://localhost:8080/api/v1/health
```

Resposta esperada:

```json
{
  "status": "UP"
}
```

## Criar Família e Responsável

```powershell
$body = @{
    familyName = "Minha Família"
    name = "João Silva"
    email = "joao@email.com"
    password = "senha123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/register-family" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

Resposta esperada:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer"
}
```

## Usar o Token

```powershell
$headers = @{
    Authorization = "Bearer eyJhbGciOiJIUzI1NiIs..."
}

Invoke-RestMethod -Uri "http://localhost:8080/api/v1/me" `
    -Method Get `
    -Headers $headers
```

## Variáveis de Ambiente

| Variável | Descrição | Valor Padrão |
|----------|-----------|--------------|
| `DB_HOST` | Host do PostgreSQL | `localhost` |
| `DB_PORT` | Porta do PostgreSQL | `5432` |
| `DB_NAME` | Nome do banco | `controlei` |
| `DB_USER` | Usuário do banco | `controlei` |
| `DB_PASSWORD` | Senha do banco | `controlei123` |
| `JWT_SECRET` | Chave secreta JWT | (obrigatório) |
| `JWT_EXPIRATION_HOURS` | Horas de expiração do token | `24` |
| `SERVER_PORT` | Porta do servidor | `8080` |

## Parar o Banco

```powershell
docker compose down
```

Para remover os dados:

```powershell
docker compose down -v
```

## Rodar Testes

```powershell
cd back
.\mvnw.cmd test
```
