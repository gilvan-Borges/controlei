# TASK-004 - Auditoria e Soft Delete

## Objetivo

Criar a base tecnica para auditoria e exclusao logica nas entidades de negocio.

## Dependencias

- TASK-001 concluida.
- TASK-002 concluida.

## Escopo

- Criar classe base ou embeddable de auditoria.
- Configurar auditoria JPA.
- Definir campos de soft delete.
- Preparar padrao para repositories filtrarem registros ativos.

## Campos Obrigatorios

Entidades principais devem possuir:

- `created_at`
- `created_by`
- `updated_at`
- `updated_by`
- `deleted_at`
- `deleted_by`

## Passos

1. Criar pacote `common.audit`.
2. Criar classe base, por exemplo `AuditableEntity`.
3. Configurar auditoria do Spring Data JPA.
4. Criar provider para usuario atual.
5. Enquanto seguranca nao estiver pronta, permitir usuario sistemico ou nulo em ambiente inicial.
6. Definir estrategia de soft delete:
   - Campo `deletedAt`
   - Campo `deletedBy`
7. Garantir que consultas de negocio ignorem registros removidos.
8. Documentar no codigo como aplicar soft delete nas entidades.

## Decisoes

Soft delete deve ser feito por atualizacao de campos, nao por delete fisico.

Repositories e queries devem filtrar `deleted_at is null`.

## Fora de Escopo

- Implementar todas as entidades de negocio.
- Implementar JWT.

## Criterios de Aceite

- Existe estrutura reutilizavel de auditoria.
- Entidades futuras conseguem herdar ou embutir auditoria.
- Existe convencao clara para soft delete.
- Testes demonstram preenchimento de campos de criacao/atualizacao quando possivel.

## Testes Esperados

- Teste de auditoria em entidade simples ou entidade de teste.
- Teste garantindo que soft delete nao remove fisicamente o registro.

## Observacoes para o Agente

Evite acoplar auditoria fortemente ao JWT nesta task. A integracao com usuario autenticado sera refinada depois da seguranca.
