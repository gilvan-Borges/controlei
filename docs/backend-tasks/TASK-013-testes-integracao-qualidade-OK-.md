# TASK-013 - Testes de Integracao e Qualidade

## Objetivo

Consolidar testes automaticos do backend e melhorar confiabilidade antes de evolucoes maiores.

## Dependencias

- TASK-012 concluida.

## Escopo

- Revisar cobertura dos fluxos principais.
- Criar testes de integracao para API.
- Criar testes de repository/migration.
- Garantir que regras de permissao estejam testadas.

## Fluxos Obrigatorios

- Cadastro de familia com responsavel.
- Login.
- Criacao de membro.
- Criacao de conta.
- Criacao de categoria.
- Criacao de receita.
- Criacao de despesa.
- Criacao de divida com parcelas.
- Pagamento de parcela.
- Criacao de investimento.
- Dashboard individual.
- Dashboard familiar.

## Testes de Seguranca Obrigatorios

- Membro nao edita transacao de outro usuario.
- Membro nao paga parcela de outro usuario.
- Membro nao edita investimento de outro usuario.
- Responsavel edita dados de membro.
- Usuario de uma familia nao acessa dados de outra.

## Qualidade

Verificar:

- Build limpo.
- Sem stack trace em resposta de erro.
- Sem senha em logs ou responses.
- Migrations rodam em banco limpo.
- Soft delete respeitado em listagens.
- Paginacao em listagens relevantes.

## Ferramentas Sugeridas

- JUnit 5.
- Spring Boot Test.
- Testcontainers para PostgreSQL, se viavel.
- MockMvc ou WebTestClient.

## Criterios de Aceite

- Suite de testes cobre fluxos principais.
- Testes rodam localmente por comando unico.
- Falhas de autorizacao estao cobertas.
- Banco de teste e isolado.
- Migrations sao exercitadas nos testes.

## Observacoes para o Agente

Se Testcontainers nao for viavel no ambiente, documente o motivo e use alternativa local com profile de teste. Nao deixe testes apontando para banco de desenvolvimento sem isolamento.
