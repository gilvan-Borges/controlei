# Controlei Frontend

Frontend Angular do Controlei, com Bootstrap, organizacao por modulos e experiencia mobile first.

## Requisitos

- Node.js compativel com Angular 21.
- npm.
- Backend rodando em `http://localhost:8080`.

## Instalar dependencias

PowerShell:

```powershell
npm install
```

## Rodar localmente

PowerShell:

```powershell
npm start
```

A aplicacao abre em:

```text
http://localhost:4200
```

## API

O ambiente local usa:

```text
src/environments/environment.ts
apiUrl: http://localhost:8080/api/v1
```

O build de producao usa:

```text
src/environments/environment.prod.ts
apiUrl: /api/v1
```

## Testes

PowerShell:

```powershell
npm test -- --watch=false
```

## Build de producao

PowerShell:

```powershell
npm run build
```

Saida do build:

```text
front/dist/front
```

Arquivos estaticos para servir em Nginx/CDN:

```text
front/dist/front/browser
```

## Docker

Build da imagem:

```powershell
docker build -t controlei-front .
```

Rodar localmente:

```powershell
docker run --rm -p 4200:80 controlei-front
```

## Usuarios locais de teste

So em desenvolvimento: com o backend no profile `local` e `APP_SEED_ENABLED=true`, o seeder cria as contas abaixo.
O botao de acesso rapido da tela de login existe apenas no build de desenvolvimento (as credenciais ficam em
`src/environments/environment.ts`, que o build de producao nao inclui). Em producao nao ha seed nem conta padrao.

```text
superadmin@controlei.local / Controlei@123
gilvan.borges@controlei.local / Controlei@123
```
