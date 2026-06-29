# TASK-011 - Qualidade, Testes e Acessibilidade

## Objetivo

Consolidar qualidade do frontend, cobrindo fluxos principais, acessibilidade basica e confiabilidade antes de evolucoes futuras.

## Dependencias

- TASK-010 concluida.

## Escopo

- Revisar services de API.
- Revisar guards e interceptors.
- Criar testes dos formularios principais.
- Criar testes dos fluxos mais importantes.
- Revisar acessibilidade basica.
- Revisar responsividade mobile.
- Garantir build limpo.

## Fluxos Obrigatorios

- Login.
- Dashboard individual.
- Dashboard familiar.
- Criacao de receita.
- Criacao de despesa.
- Criacao de divida parcelada.
- Pagamento de parcela.
- Criacao de investimento.
- Criacao de conta.
- Criacao de categoria.

## Qualidade

Verificar:

- Build limpo.
- Testes rodam por comando unico.
- Sem chamadas HTTP diretas em componentes.
- Sem token exposto em logs.
- Mensagens de erro amigaveis.
- Loading e vazio em listagens.
- Formularios com labels.
- Botoes com area de toque confortavel.
- Layout sem overflow horizontal no mobile.

## Criterios de Aceite

- Suite de testes cobre services e fluxos principais.
- Build passa.
- Aplicacao e navegavel no mobile.
- Listagens principais tem loading, erro e vazio.
- Formularios principais tem validacao clara.

## Testes Esperados

- Services de API.
- Guard de autenticacao.
- Interceptor JWT.
- Formularios de transacao, divida e investimento.
- Alternancia de dashboard individual/familiar.

## Observacoes para o Agente

Se algum teste E2E nao for viavel agora, documente o motivo e mantenha testes unitarios/integracao de componentes cobrindo o comportamento essencial.
