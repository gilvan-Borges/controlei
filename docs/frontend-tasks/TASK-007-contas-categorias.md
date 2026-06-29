# TASK-007 - Contas e Categorias

## Objetivo

Implementar telas de CRUD para contas financeiras e categorias.

## Dependencias

- TASK-006 concluida.

## Escopo

- Criar `AccountService`.
- Criar `CategoryService`.
- Criar listagem de contas.
- Criar formulario de conta.
- Criar listagem de categorias.
- Criar formulario de categoria.
- Criar exclusao com confirmacao.
- Criar filtros basicos.

## API

```text
GET    /api/v1/accounts
POST   /api/v1/accounts
GET    /api/v1/accounts/{id}
PUT    /api/v1/accounts/{id}
DELETE /api/v1/accounts/{id}

GET    /api/v1/categories
POST   /api/v1/categories
GET    /api/v1/categories/{id}
PUT    /api/v1/categories/{id}
DELETE /api/v1/categories/{id}
```

## Regras

- Conta compartilhada nao deve exigir usuario dono.
- Conta individual deve exigir usuario.
- Categoria deve exigir tipo.
- Confirmar exclusao.
- Formularios devem validar obrigatorios no frontend.
- Listagens devem prever loading, vazio, erro e filtros sem resultado.

## Filtros

Contas:

- Ativa.
- Tipo.
- Compartilhada.
- Usuario.

Categorias:

- Ativa.
- Tipo.

## Criterios de Aceite

- CRUD de contas funciona.
- CRUD de categorias funciona.
- Soft delete e acionado via delete da API.
- Duplicidade de categoria mostra erro amigavel.
- Tela e usavel no mobile.

## Testes Esperados

- Services chamam endpoints corretos.
- Formularios validam campos obrigatorios.
- Exclusao exige confirmacao.
- Erro de duplicidade e exibido ao usuario.

## Observacoes para o Agente

Evite tabela larga no mobile. Prefira cards/listas compactas.
