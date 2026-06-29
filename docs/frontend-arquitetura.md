# Controlei - Arquitetura Frontend

## Stack

- Angular atual.
- Bootstrap.
- TypeScript.
- RxJS.
- Angular Router.

## Direcao de Interface

O frontend deve ser responsivo e priorizar a experiencia mobile.

No celular, o sistema deve parecer um app nativo, com navegacao inferior fixa para as areas principais.

Exemplos de atalhos na barra inferior:

- Inicio.
- Transacoes.
- Dividas.
- Investimentos.
- Perfil.

Em desktop, a navegacao pode se adaptar para menu lateral ou superior, mantendo a mesma organizacao de rotas.

## Componentizacao

O projeto deve usar componentes reutilizaveis para telas, formularios e elementos de interface.

Como decisao inicial, nao usar arquitetura baseada em standalone components. Usar organizacao por modulos Angular, caso o projeto Angular criado permita essa abordagem.

Componentes comuns sugeridos:

- Botao de acao.
- Campo monetario.
- Campo de data.
- Seletor de usuario.
- Seletor de categoria.
- Card de resumo financeiro.
- Lista de transacoes.
- Lista de parcelas.
- Modal de confirmacao.
- Estado vazio.
- Loading.
- Mensagem de erro.

## Organizacao do Projeto

Estrutura sugerida:

```text
front/
  src/app/
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

## Core

O modulo `core` deve conter servicos globais e recursos usados pela aplicacao inteira:

- Autenticacao.
- Interceptor HTTP para JWT.
- Interceptor de erros.
- Guards de rota.
- Servico de usuario logado.
- Models globais.

## Shared

O modulo `shared` deve conter componentes, pipes e diretivas reutilizaveis.

Nao colocar regra de negocio especifica dentro do `shared`.

## Features

Cada funcionalidade principal deve ficar isolada em sua propria pasta.

Exemplos:

- `dashboard`
- `transactions`
- `debts`
- `installments`
- `investments`
- `accounts`
- `categories`
- `users`

Cada feature pode ter:

- Pages.
- Components.
- Services.
- Models.
- Routes.

## Rotas Sugeridas

```text
/login
/app
/app/dashboard
/app/transactions
/app/debts
/app/debts/:id
/app/installments
/app/investments
/app/accounts
/app/categories
/app/users
/app/profile
```

## Visoes

### Dashboard Individual

Deve mostrar os dados do usuario logado:

- Saldo.
- Receitas do mes.
- Despesas do mes.
- Dividas em aberto.
- Proximas parcelas.
- Investimentos.

### Dashboard Familiar

Deve mostrar o consolidado familiar:

- Total da familia.
- Detalhamento por usuario.
- Despesas por usuario.
- Dividas por usuario.
- Parcelas futuras por usuario.
- Investimentos por usuario.

O usuario deve conseguir alternar entre visao individual e familiar de forma simples.

## Formularios

Formularios devem ser simples e objetivos.

Regras:

- Validar campos obrigatorios no frontend.
- Mostrar mensagens claras em portugues.
- Usar mascara ou componente especifico para dinheiro.
- Usar seletor de data adequado para mobile.
- Evitar formularios longos demais em uma unica tela.
- Confirmar exclusoes.

## Experiencia Mobile

Prioridades:

- Barra inferior fixa.
- Botoes com area de toque confortavel.
- Telas com rolagem natural.
- Acoes principais visiveis.
- Listas faceis de escanear.
- Filtros simples por periodo, usuario, categoria e status.

Evitar:

- Tabelas largas no celular.
- Menus complexos.
- Formularios com excesso de campos.

## Bootstrap

Bootstrap deve ser usado como base visual e de responsividade.

Padroes:

- Grid do Bootstrap para layouts.
- Utilitarios para espacamento.
- Formularios padronizados.
- Modais quando fizer sentido.
- Cores customizadas do projeto podem ser definidas por variaveis/scss.

## Comunicacao com API

Todas as chamadas HTTP devem passar por services.

Evitar chamadas diretas nos componentes.

Padrao sugerido:

```text
DebtService
TransactionService
InvestmentService
DashboardService
```

O token JWT deve ser enviado por interceptor.

Erros globais devem ser tratados por interceptor ou servico centralizado, com mensagens amigaveis para o usuario.

## Models

Criar interfaces TypeScript alinhadas aos DTOs da API.

Exemplo:

```ts
export interface Debt {
  id: string;
  userId: string;
  description: string;
  totalAmount: number;
  installmentCount: number;
  installmentAmount: number;
  status: string;
}
```

## Autorizacao no Frontend

O frontend deve:

- Esconder ou desabilitar acoes que o usuario nao pode executar.
- Permitir visualizacao familiar para todos.
- Permitir edicao de registros de outros usuarios apenas para responsavel.

Mesmo assim, a autorizacao real deve ser garantida pelo backend.

## Estados de Tela

Toda tela de listagem deve prever:

- Loading.
- Lista vazia.
- Erro ao carregar.
- Dados carregados.
- Filtros sem resultado.

Toda acao de salvar deve prever:

- Salvando.
- Sucesso.
- Erro de validacao.
- Erro inesperado.

## Padroes de Texto

- Interface em portugues.
- Mensagens curtas e claras.
- Usar termos simples: gasto, receita, divida, parcela, investimento.
- Evitar termos tecnicos financeiros quando nao forem necessarios.

## Testes

Prioridades:

- Services de API.
- Guards de autenticacao.
- Componentes de formulario principais.
- Fluxo de criacao de divida parcelada.
- Alternancia entre visao individual e familiar.

## Pontos Futuros

- PWA.
- Notificacoes de vencimento.
- Cartao de credito e faturas.
- Graficos mais avancados.
- Modo offline.
