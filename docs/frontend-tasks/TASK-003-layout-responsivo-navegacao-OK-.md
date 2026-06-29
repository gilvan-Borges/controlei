# TASK-003 - Layout Responsivo e Navegacao Principal

## Objetivo

Criar a experiencia principal da aplicacao com foco mobile, usando barra inferior fixa no celular e layout adequado para desktop.

## Dependencias

- TASK-002 concluida.

## Escopo

- Criar `MobileShell`.
- Criar `DesktopShell`.
- Criar `BottomNav`.
- Criar navegacao principal.
- Ajustar espacamentos, areas de toque e responsividade.
- Criar estado visual de rota ativa.

## Rotas Principais

```text
/app/dashboard
/app/transactions
/app/debts
/app/installments
/app/investments
/app/profile
```

## Regras

- Mobile first.
- Barra inferior fixa no celular.
- Acoes principais sempre acessiveis.
- Evitar tabelas largas no celular.
- Usar Bootstrap como base visual.
- Manter layout leve e performatico.

## Criterios de Aceite

- Navegacao inferior aparece no mobile.
- Desktop usa layout adaptado sem prejudicar mobile.
- Rotas principais navegam corretamente.
- Conteudo nao fica escondido atras da barra inferior.
- Layout funciona em larguras pequenas.

## Testes Esperados

- Shell renderiza com usuario autenticado.
- Navegacao exibe item ativo.
- Rotas privadas usam layout correto.

## Observacoes para o Agente

O sistema deve parecer um app no celular. Priorize clareza, toque confortavel e rolagem natural.
