# TASK-10: Relatórios Gerenciais, Exportação em PDF/Excel e Relatório para IRPF

## 🎯 Objetivo
Permitir a geração e exportação de relatórios financeiros detalhados em formatos PDF e planilhas Excel (XLSX/CSV), além de um módulo dedicado para apoio na declaração de Imposto de Renda (Bens e Direitos, Rendimentos e Dívidas).

---

## 📋 Requisitos Técnicos

### 1. Bibliotecas e Ferramentas
- Backend: **Apache POI** (para geração de planilhas Excel) e **OpenPDF / iText** (para documentos em PDF).
- Endpoint assíncrono para relatórios pesados ou download direto via stream.

### 2. Tipos de Relatórios
- **Extrato Financeiro Mensal (PDF/Excel):**
  - Todas as receitas e despesas do período com agrupamento por membro e categoria.
- **Relatório de Evolução Patrimonial (PDF):**
  - Gráficos de saldo acumulado, dívidas em amortização e patrimônio investido mês a mês.
- **Relatório de Apoio ao Imposto de Renda (IRPF):**
  - Seção 1: Saldos em contas em 31/12 do ano anterior e 31/12 do ano corrente.
  - Seção 2: Posição de investimentos (custo de aquisição acumulado).
  - Seção 3: Dívidas e ônus reais em aberto.

### 3. Endpoints REST
- `GET /api/v1/reports/monthly-statement?year=2026&month=8&format=pdf`
- `GET /api/v1/reports/monthly-statement?year=2026&month=8&format=xlsx`
- `GET /api/v1/reports/tax-declaration?year=2025`

---

## 🧪 Critérios de Aceite e Testes
1. O PDF deve conter cabeçalho personalizado da família, data de emissão e formatação monetária correta (R$ #.##0,00).
2. Validação estrita de permissões (Membro só exporta o individual; Responsável exporta o individual ou familiar).
