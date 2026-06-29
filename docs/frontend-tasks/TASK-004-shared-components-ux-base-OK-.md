# TASK-004 - Componentes Compartilhados e UX Base

## Objetivo

Criar componentes reutilizaveis para formularios, listas, estados de tela e acoes comuns.

## Dependencias

- TASK-003 concluida.

## Escopo

- Criar componentes compartilhados.
- Criar pipes para moeda e data, se necessario.
- Criar padrao de mensagens de erro.
- Criar modal de confirmacao.
- Criar loading e estado vazio.

## Componentes Obrigatorios

- Botao de acao.
- Campo monetario.
- Campo de data.
- Seletor de usuario.
- Seletor de categoria.
- Card de resumo financeiro.
- Modal de confirmacao.
- Estado vazio.
- Loading.
- Mensagem de erro.

## Regras

- `shared` nao deve conter regra de negocio especifica.
- Componentes devem ser reutilizaveis entre features.
- Mensagens em portugues.
- Inputs devem suportar validacao visual.
- Componentes devem funcionar bem no mobile.

## Criterios de Aceite

- Componentes compartilhados criados.
- Features futuras conseguem reutilizar os componentes.
- Loading, erro e vazio tem padrao unico.
- Campos monetarios exibem BRL corretamente.
- Datas exibem formato pt-BR.

## Testes Esperados

- Renderizacao dos principais componentes.
- Campo monetario formata valor.
- Modal confirma e cancela acao.

## Observacoes para o Agente

Evite criar componentes genericos demais. Crie apenas o que sera usado nas proximas tasks.
