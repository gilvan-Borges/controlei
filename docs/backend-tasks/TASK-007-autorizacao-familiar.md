# TASK-007 - Autorizacao Familiar

## Objetivo

Centralizar as regras de permissao familiar para leitura e escrita.

## Dependencias

- TASK-006 concluida.

## Escopo

- Criar servico de autorizacao.
- Garantir isolamento por familia.
- Garantir regra de escrita por dono/responsavel.
- Aplicar a regra nos modulos existentes.

## Regras

Leitura:

- Todo usuario autenticado pode visualizar dados da propria familia.
- Nenhum usuario pode visualizar dados de outra familia.

Escrita:

- Responsavel pode editar dados de qualquer usuario da familia.
- Membro comum pode editar apenas registros cujo `user_id` seja o seu.

## Implementacao Sugerida

Criar `AuthorizationService` com metodos como:

```text
requireSameFamily(resourceFamilyId)
requireCanWrite(resourceFamilyId, resourceUserId)
isResponsible()
currentUserId()
currentFamilyId()
```

## Passos

1. Criar objeto de contexto autenticado.
2. Criar `AuthorizationService`.
3. Criar testes unitarios para todas as combinacoes de role.
4. Aplicar nas operacoes de usuario/familia ja existentes.
5. Garantir que endpoints nunca confiem em `familyId` enviado pelo cliente.

## Fora de Escopo

- Implementar modulos financeiros.
- Criar permissoes granulares.

## Criterios de Aceite

- Existe regra centralizada de autorizacao.
- Membro comum nao edita outro usuario.
- Responsavel edita membros da familia.
- Usuario nao acessa outra familia.
- Services usam `familyId` do token/contexto, nao do request.

## Testes Esperados

- Responsavel edita membro.
- Membro tenta editar outro membro e recebe 403.
- Usuario tenta acessar recurso de outra familia e recebe 403 ou 404, conforme padrao escolhido.

## Observacoes para o Agente

Prefira retornar 404 para recursos de outra familia quando isso reduzir vazamento de informacao. Seja consistente no projeto.
