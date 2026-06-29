# Controlei - Arquitetura Geral

## Objetivo

O Controlei e um sistema para controle da vida financeira familiar. Ele deve permitir que uma familia acompanhe receitas, despesas, dividas parceladas, compras, investimentos e transacoes, mantendo sempre duas perspectivas principais:

- Visao individual: dados financeiros do usuario logado.
- Visao familiar: consolidado da familia, com detalhamento por usuario.

O sistema deve ser simples de usar, responsivo e pensado primeiro para uso em celular.

## Decisoes Principais

- Backend em Java com Spring Boot.
- Banco de dados PostgreSQL.
- Migrations desde o inicio com Flyway.
- Frontend em Angular atual.
- Interface com Bootstrap.
- Autenticacao com JWT.
- Dados financeiros sempre associados a uma familia e a um usuario.
- Auditoria em todas as entidades principais.
- Soft delete em entidades de negocio.
- Tratamento padronizado de erros.
- Localizacao inicial para Brasil, com moeda BRL e datas em formato pt-BR.

## Modelo Familiar

Cada usuario pertence a apenas uma familia.

Cada familia possui um usuario responsavel. O responsavel tem permissao para visualizar e editar os dados de todos os usuarios da familia.

Usuarios comuns podem visualizar todos os dados da familia, mas so podem editar os proprios registros.

Nao havera, neste primeiro momento, niveis diferentes de permissao entre usuarios comuns.

## Visoes do Sistema

### Visao Individual

Mostra apenas os dados financeiros vinculados ao usuario logado.

Exemplos:

- Minhas receitas.
- Minhas despesas.
- Minhas dividas.
- Minhas parcelas.
- Meus investimentos.
- Meu saldo previsto.

### Visao Familiar

Mostra os dados consolidados de todos os usuarios da familia, sempre com detalhamento por usuario.

Exemplos:

- Total de despesas da familia no mes.
- Quanto cada usuario gastou.
- Total de dividas em aberto por usuario.
- Parcelas futuras da familia.
- Investimentos por usuario e total familiar.

### Contas Individuais e Compartilhadas

O sistema deve permitir a separacao entre:

- Contas individuais: pertencem a um usuario especifico.
- Contas compartilhadas: fazem parte da organizacao financeira familiar.

Mesmo quando uma conta for compartilhada, toda movimentacao deve registrar o usuario responsavel pelo lancamento.

## Modulos do Produto

Primeira versao:

- Familias.
- Usuarios.
- Contas.
- Categorias.
- Transacoes.
- Receitas.
- Despesas.
- Compras.
- Dividas parceladas.
- Parcelas.
- Investimentos simples.
- Dashboard individual.
- Dashboard familiar.

Futuro:

- Cartao de credito.
- Fechamento e vencimento de fatura.
- Rendimento automatico de investimentos.
- Aportes, resgates e historico detalhado de investimentos.

## Dividas e Parcelas

Divida e qualquer registro financeiro parcelado cadastrado pelo usuario.

Exemplo:

- Compra: Geladeira.
- Data da compra: 29/06/2026.
- Valor total: R$ 3.000,00.
- Quantidade de parcelas: 10.
- Valor de cada parcela: R$ 300,00.

Ao cadastrar uma divida parcelada, o sistema deve gerar automaticamente todas as parcelas futuras.

Compras e despesas comuns nao precisam gerar parcelas, exceto quando forem registradas como divida.

## Investimentos

Na primeira versao, investimentos serao controlados de forma simples e manual.

O usuario deve poder cadastrar:

- Nome do investimento.
- Tipo.
- Valor aplicado ou saldo atual.
- Data.
- Observacoes.

Nao havera, inicialmente, calculo automatico de rendimento, aportes, resgates ou integracao com instituicoes financeiras.

## Padroes de Dados

Todas as entidades principais devem possuir:

- Identificador unico.
- `familyId`.
- `userId`, quando aplicavel.
- Data de criacao.
- Usuario que criou.
- Data de ultima atualizacao.
- Usuario que atualizou.
- Indicador de exclusao logica.

Entidades financeiras devem possuir, quando fizer sentido:

- Valor.
- Data de competencia.
- Data de vencimento.
- Data de pagamento.
- Status.
- Categoria.
- Descricao.
- Observacoes.

## Permissoes

Regras gerais:

- Todo usuario autenticado pode visualizar todos os dados da sua familia.
- Usuario comum so pode criar, editar e excluir os proprios registros.
- Usuario responsavel pode criar, editar e excluir registros de qualquer usuario da familia.
- Nenhum usuario pode acessar dados de outra familia.

Essas regras devem ser aplicadas no backend. O frontend pode esconder ou desabilitar acoes, mas nao deve ser a unica camada de seguranca.

## API

Padroes gerais:

- API REST.
- Rotas versionadas com prefixo `/api/v1`.
- JSON como formato padrao.
- Datas em ISO-8601 na comunicacao com a API.
- Valores monetarios representados como decimal, nunca ponto flutuante.
- Paginacao em listagens grandes.
- Filtros por periodo, usuario, categoria, status e tipo quando fizer sentido.

Exemplo de erro padronizado:

```json
{
  "timestamp": "2026-06-29T10:00:00",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Dados invalidos",
  "path": "/api/v1/transactions",
  "fields": [
    {
      "field": "amount",
      "message": "Valor deve ser maior que zero"
    }
  ]
}
```

## Boas Praticas Comuns

- Validar dados no frontend para melhorar a experiencia do usuario.
- Validar novamente no backend para garantir seguranca e consistencia.
- Centralizar regras de permissao no backend.
- Evitar duplicacao de regra de negocio entre front e back.
- Usar nomes consistentes entre API, banco e telas.
- Manter respostas da API previsiveis.
- Nao expor dados internos ou stack trace para o usuario.
- Registrar logs estruturados no backend.
- Criar migrations para toda alteracao de banco.
- Nunca alterar migration ja aplicada; criar nova migration.
- Usar componentes de interface consistentes no frontend.
- Pensar primeiro no uso mobile.

## Nomenclatura

Padrao sugerido:

- Backend: nomes em ingles no codigo, entidades e API.
- Banco: tabelas e colunas em `snake_case`.
- Frontend: codigo em ingles, textos de tela em portugues.
- Documentacao: portugues.

Exemplos:

- `family`
- `family_member`
- `transaction`
- `debt`
- `installment`
- `investment`

## Status Padrao

Status financeiros sugeridos:

- `PENDING`: pendente.
- `PAID`: pago.
- `OVERDUE`: vencido.
- `CANCELED`: cancelado.

Tipos de transacao sugeridos:

- `INCOME`: receita.
- `EXPENSE`: despesa.
- `TRANSFER`: transferencia.

## Seguranca

- Login com JWT.
- Senhas armazenadas com hash seguro.
- Refresh token pode ser avaliado em etapa posterior.
- Todas as rotas privadas devem exigir autenticacao.
- O backend deve validar `familyId` e `userId` em todas as operacoes sensiveis.
- O usuario nao deve conseguir informar manualmente outro `familyId` para acessar dados externos.

## Auditoria e Exclusao Logica

Todas as entidades principais devem registrar:

- Quem criou.
- Quando criou.
- Quem atualizou.
- Quando atualizou.
- Quando foi removido logicamente.
- Quem removeu logicamente.

Exclusoes de negocio devem ser soft delete. Dados financeiros nao devem ser removidos fisicamente em operacoes normais.

## Responsividade

O sistema deve ser usavel em desktop, tablet e celular, com prioridade para celular.

No celular, a navegacao principal deve parecer nativa, usando barra inferior com atalhos para as principais areas do sistema.

## Documentos Relacionados

- [Modelo de Dominio](modelo-dominio.md)
- [Arquitetura Backend](backend-arquitetura.md)
- [Arquitetura Frontend](frontend-arquitetura.md)
