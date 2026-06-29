# TASK-003 - Erros, Validacao e Observabilidade Inicial

## Objetivo

Criar o padrao global de tratamento de erros, validacao de entrada e logs basicos.

## Dependencias

- TASK-001 concluida.
- TASK-002 recomendada.

## Escopo

- Criar modelo padrao de erro da API.
- Criar `GlobalExceptionHandler`.
- Tratar erros de validacao.
- Criar excecoes de negocio.
- Configurar logs basicos.

## Passos

1. Criar pacote `common.error`.
2. Criar DTO de erro:
   - `timestamp`
   - `status`
   - `error`
   - `message`
   - `path`
   - `fields`
3. Criar DTO para erro de campo:
   - `field`
   - `message`
4. Criar excecoes:
   - `BusinessException`
   - `NotFoundException`
   - `ForbiddenException`
   - `UnauthorizedException`
5. Criar enum ou constantes de codigos de erro:
   - `VALIDATION_ERROR`
   - `BUSINESS_ERROR`
   - `NOT_FOUND`
   - `FORBIDDEN`
   - `UNAUTHORIZED`
   - `INTERNAL_ERROR`
6. Implementar `GlobalExceptionHandler`.
7. Tratar:
   - Bean Validation
   - JSON invalido
   - parametro invalido
   - recurso nao encontrado
   - acesso negado
   - erro inesperado
8. Garantir que stack trace nao seja exposto na resposta.
9. Configurar logs para erros inesperados.

## Fora de Escopo

- Implementar autenticacao.
- Implementar dominio financeiro.

## Criterios de Aceite

- Erros de validacao retornam status 400 e lista de campos.
- Recurso nao encontrado retorna 404.
- Acesso negado retorna 403.
- Erro inesperado retorna 500 sem detalhes internos.
- O formato segue `docs/arquitetura-geral.md`.

## Testes Esperados

- Teste de DTO invalido em endpoint de exemplo ou teste.
- Teste de excecao `NotFoundException`.
- Teste de excecao `BusinessException`.

## Observacoes para o Agente

Se ainda nao houver endpoints reais, crie endpoints de teste apenas no escopo de teste, nao controllers publicos desnecessarios.
