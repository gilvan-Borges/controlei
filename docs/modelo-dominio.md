# Controlei - Modelo de Dominio

## Visao Geral

O dominio do Controlei gira em torno de uma familia, seus usuarios e seus registros financeiros. Todo dado financeiro deve estar protegido pelo contexto da familia e, quando aplicavel, vinculado ao usuario dono do registro.

## Entidades Principais

### Family

Representa o grupo familiar.

Campos sugeridos:

- `id`
- `name`
- `responsibleUserId`
- `createdAt`
- `createdBy`
- `updatedAt`
- `updatedBy`
- `deletedAt`
- `deletedBy`

Regras:

- Uma familia tem um unico responsavel.
- Um usuario pertence a apenas uma familia.
- Dados de uma familia nao podem ser acessados por usuarios de outra familia.

### User

Representa uma pessoa que acessa o sistema.

Campos sugeridos:

- `id`
- `familyId`
- `name`
- `email`
- `passwordHash`
- `role`
- `active`
- `createdAt`
- `createdBy`
- `updatedAt`
- `updatedBy`
- `deletedAt`
- `deletedBy`

Roles sugeridas:

- `RESPONSIBLE`
- `MEMBER`

Regras:

- Usuario responsavel pode editar registros de todos os membros da familia.
- Usuario comum pode editar apenas os proprios registros.
- Todos podem visualizar os dados da familia.

### Account

Representa uma conta financeira individual ou compartilhada.

Campos sugeridos:

- `id`
- `familyId`
- `userId`
- `name`
- `type`
- `shared`
- `initialBalance`
- `active`
- `createdAt`
- `createdBy`
- `updatedAt`
- `updatedBy`
- `deletedAt`
- `deletedBy`

Tipos sugeridos:

- `CHECKING`
- `SAVINGS`
- `CASH`
- `INVESTMENT`
- `OTHER`

Regras:

- Uma conta pode ser individual ou compartilhada.
- Mesmo em conta compartilhada, os lancamentos devem registrar o usuario responsavel.

### Category

Representa categorias de receitas, despesas, dividas e investimentos.

Campos sugeridos:

- `id`
- `familyId`
- `name`
- `type`
- `color`
- `icon`
- `active`
- `createdAt`
- `createdBy`
- `updatedAt`
- `updatedBy`
- `deletedAt`
- `deletedBy`

Tipos sugeridos:

- `INCOME`
- `EXPENSE`
- `DEBT`
- `INVESTMENT`

### Transaction

Representa movimentacoes financeiras simples, como receitas, despesas e transferencias.

Campos sugeridos:

- `id`
- `familyId`
- `userId`
- `accountId`
- `categoryId`
- `type`
- `description`
- `amount`
- `transactionDate`
- `dueDate`
- `paidAt`
- `status`
- `notes`
- `createdAt`
- `createdBy`
- `updatedAt`
- `updatedBy`
- `deletedAt`
- `deletedBy`

Tipos:

- `INCOME`
- `EXPENSE`
- `TRANSFER`

Status:

- `PENDING`
- `PAID`
- `OVERDUE`
- `CANCELED`

Regras:

- Valores devem ser maiores que zero.
- O tipo define se o valor entra ou sai do saldo.
- Transferencias devem preservar rastreabilidade entre conta origem e destino.

### Debt

Representa uma divida parcelada cadastrada pelo usuario.

Campos sugeridos:

- `id`
- `familyId`
- `userId`
- `categoryId`
- `description`
- `purchaseDate`
- `totalAmount`
- `installmentCount`
- `installmentAmount`
- `status`
- `notes`
- `createdAt`
- `createdBy`
- `updatedAt`
- `updatedBy`
- `deletedAt`
- `deletedBy`

Regras:

- Ao criar uma divida, o sistema deve gerar as parcelas automaticamente.
- A soma das parcelas deve bater com o valor total.
- A divida deve refletir o status das parcelas.

### Installment

Representa cada parcela de uma divida.

Campos sugeridos:

- `id`
- `familyId`
- `userId`
- `debtId`
- `installmentNumber`
- `amount`
- `dueDate`
- `paidAt`
- `status`
- `createdAt`
- `createdBy`
- `updatedAt`
- `updatedBy`
- `deletedAt`
- `deletedBy`

Regras:

- Parcelas sao geradas a partir da divida.
- Cada parcela deve ter numero sequencial.
- O pagamento de parcelas deve atualizar o resumo da divida.
- Parcelas futuras devem aparecer no planejamento financeiro.

### Investment

Representa um investimento simples controlado manualmente.

Campos sugeridos:

- `id`
- `familyId`
- `userId`
- `categoryId`
- `name`
- `type`
- `currentAmount`
- `referenceDate`
- `notes`
- `createdAt`
- `createdBy`
- `updatedAt`
- `updatedBy`
- `deletedAt`
- `deletedBy`

Tipos sugeridos:

- `SAVINGS`
- `FIXED_INCOME`
- `STOCK`
- `FUND`
- `CRYPTO`
- `OTHER`

Regras:

- Na primeira versao, o saldo sera informado manualmente.
- Nao havera calculo automatico de rendimento.

## Regras de Acesso por Entidade

Todas as consultas devem filtrar por `familyId`.

Para escrita:

- Responsavel pode criar, editar e excluir dados de qualquer usuario da familia.
- Membro pode criar, editar e excluir apenas registros onde `userId` seja igual ao seu proprio usuario.

Para leitura:

- Todos os membros autenticados podem visualizar todos os dados da familia.

## Relacionamentos

- `Family` possui muitos `User`.
- `Family` possui muitas `Account`.
- `Family` possui muitas `Category`.
- `User` possui muitas `Transaction`.
- `User` possui muitas `Debt`.
- `Debt` possui muitas `Installment`.
- `User` possui muitos `Investment`.

## Dashboards

### Dashboard Individual

Deve mostrar:

- Saldo individual.
- Receitas do periodo.
- Despesas do periodo.
- Dividas em aberto.
- Parcelas proximas.
- Investimentos do usuario.

### Dashboard Familiar

Deve mostrar:

- Saldo consolidado da familia.
- Receitas totais por usuario.
- Despesas totais por usuario.
- Dividas totais por usuario.
- Parcelas futuras por usuario.
- Investimentos por usuario e total familiar.

## Pontos para Evolucao

- Cartao de credito.
- Faturas.
- Metas financeiras.
- Orcamento mensal por categoria.
- Recorrencia de transacoes.
- Historico detalhado de investimentos.
- Anexos em compras e dividas.
