# TASK-001 - Bootstrap do Projeto Frontend

## Objetivo

Criar a base inicial do frontend Angular do Controlei dentro da pasta `front`, usando Angular atual, Bootstrap, TypeScript, RxJS e organizacao por modulos.

## Dependencias

Nenhuma task anterior.

## Escopo

- Criar projeto Angular em `front`, caso ainda nao exista.
- Configurar Bootstrap.
- Configurar estrutura base de pastas.
- Criar shell inicial da aplicacao.
- Criar rota publica de login e rota privada `/app`.
- Garantir build e testes iniciais.

## Estrutura Obrigatoria

```text
front/src/app/
  core/
    auth/
    guards/
    interceptors/
    services/
    models/
  shared/
    components/
    pipes/
    directives/
  layout/
    mobile-shell/
    desktop-shell/
    bottom-nav/
  features/
    dashboard/
    transactions/
    debts/
    installments/
    investments/
    accounts/
    categories/
    users/
    profile/
```

## Regras

- Usar componentes e modulos Angular, nao iniciar com arquitetura baseada em standalone components.
- Textos de tela em portugues.
- Codigo, models e services em ingles.
- Mobile first.
- Nao conectar API real nesta task, exceto configuracao inicial de ambiente.

## Rotas Iniciais

```text
/login
/app
/app/dashboard
```

## Criterios de Aceite

- Projeto Angular existe dentro de `front`.
- Bootstrap esta configurado.
- Estrutura `core`, `shared`, `layout` e `features` existe.
- Aplicacao abre em rota inicial.
- Build passa.
- Testes iniciais passam.

## Testes Esperados

- Teste de criacao do app.
- Teste basico de renderizacao do shell.

## Observacoes para o Agente

Nao implemente regra financeira nesta task. O foco e fundacao limpa, responsiva e facil de evoluir.
