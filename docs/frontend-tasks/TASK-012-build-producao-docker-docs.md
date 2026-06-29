# TASK-012 - Build de Producao, Docker e Documentacao

## Objetivo

Preparar o frontend para build de producao, execucao local integrada e handoff de deploy.

## Dependencias

- TASK-011 concluida.

## Escopo

- Configurar build de producao.
- Revisar environments.
- Documentar comandos locais.
- Documentar integracao com backend.
- Criar ou ajustar Dockerfile se fizer sentido.
- Ajustar compose local se o projeto decidir servir front junto.
- Validar artefatos finais.

## Regras

- Nao commitar `.env` real.
- Nao expor URL sensivel ou segredo no bundle.
- API base URL deve vir de environment/config apropriado.
- Build de producao deve ser reproduzivel.
- Documentacao deve incluir comandos Windows/PowerShell.

## Comandos Esperados

Documentar equivalentes para:

```text
npm install
npm start
npm test
npm run build
```

## Criterios de Aceite

- Build de producao passa.
- Pasta de output e conhecida e documentada.
- README ou docs explicam como rodar o front.
- Configuracao de API esta documentada.
- Nenhum segredo real foi adicionado.

## Testes Esperados

- Build de producao.
- Testes frontend.
- Smoke manual da aplicacao rodando localmente.

## Observacoes para o Agente

Ao finalizar, informar exatamente onde ficou o build para deploy e quais comandos foram usados para validar.
