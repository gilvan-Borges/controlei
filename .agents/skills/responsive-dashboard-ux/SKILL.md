---
name: responsive-dashboard-ux
description: "Specialized UX/UI skill for modern web dashboards, SaaS interfaces, financial management systems, data visualization, high-density tables, metric cards, navigation drawers, and responsive layouts using Angular, Bootstrap 5, and CSS Design Tokens."
---

# Responsive Dashboard & SaaS UX Skill

This skill provides design patterns, interaction standards, and layout templates for building modern, high-conversion dashboards and financial/operational management applications.

## When to Apply
- Designing and implementing SaaS dashboard layouts (sidebar navigation, topbar, content area).
- Crafting KPI / Metric summary cards (totals, percentage changes, sparkline trends, icon badges).
- Creating interactive data tables with search, sorting, filtering chips, pagination, and multi-select actions.
- Implementing responsive drawer / offcanvas filters and bottom sheets for mobile.
- Designing empty states, error fallbacks, skeleton loaders, and feedback toasts.
- Building modal workflows for creation, editing, and destructive confirmations.

---

## 1. Visual Architecture & Layout Tokens

### Layout Structure (Desktop & Mobile)
```
+-------------------------------------------------------------------+
|  [Sidebar / Brand]   |  [Topbar: Breadcrumb, Search, User Avatar]  |
|  - Dashboard         +--------------------------------------------+
|  - Despesas          |  [Page Header: Title + Action CTA Buttons] |
|  - Receitas          +--------------------------------------------+
|  - Categorias        |  [KPI Metrics Cards: 4-column responsive]  |
|  - Relatórios        +--------------------------------------------+
|  - Configurações     |  [Main Data Card: Filters + Table/Charts]  |
+-------------------------------------------------------------------+
```

### Key Dimensions & Responsive Breakpoints
- **Sidebar Width**: 260px (expanded), 72px (compact icon-only), offcanvas drawer on screens `< 992px (lg)`.
- **Topbar Height**: 64px with sticky positioning (`position: sticky; top: 0; z-index: 1020; backdrop-filter: blur(12px);`).
- **Card Spacing & Padding**: `p-3 p-md-4`, `gap-3 gap-md-4`, `border-radius: 12px` (or `var(--bs-border-radius-lg)`).
- **Responsive Grid**:
  - Desktop (`≥ 1200px`): 4 KPI cards per row (`col-xl-3`).
  - Tablet (`≥ 768px`): 2 KPI cards per row (`col-md-6`).
  - Mobile (`< 768px`): 1 KPI card per row (`col-12`).

---

## 2. Component Design Standards

### A. KPI Metric Cards
Every metric card must clearly convey 4 pieces of information:
1. **Category / Title**: Muted, uppercase or medium weight (`text-muted small fw-medium`).
2. **Primary Value**: Large, scannable typography (`h3 mb-0 fw-bold font-monospace`).
3. **Trend Indicator**: Direction icon + percentage with semantic coloring (e.g. green for increase in income, red for increase in expenses).
4. **Context / Icon Badge**: Rounded icon badge with tinted background (`bg-primary-subtle text-primary p-2 rounded-3`).

#### Example HTML / Angular Template:
```html
<div class="card border-0 shadow-sm rounded-3 hover-lift">
  <div class="card-body p-3 p-md-4">
    <div class="d-flex justify-content-between align-items-start mb-2">
      <span class="text-muted small fw-medium text-uppercase">Saldo em Conta</span>
      <div class="rounded-3 p-2 bg-success-subtle text-success">
        <i class="bi bi-wallet2 fs-5" aria-hidden="true"></i>
      </div>
    </div>
    <div class="h3 fw-bold mb-2 font-monospace">R$ 14.850,00</div>
    <div class="d-flex align-items-center gap-1 small text-success">
      <i class="bi bi-arrow-up-right fw-bold"></i>
      <span class="fw-semibold">+12,5%</span>
      <span class="text-muted">vs. mês anterior</span>
    </div>
  </div>
</div>
```

### B. High-Density Data Tables with Micro-Interactions
1. **Sticky Header**: Keep table headers visible when scrolling large datasets (`position: sticky; top: 0;`).
2. **Row Hover**: Visual feedback on hover (`table-hover`) with subtle border or highlight.
3. **Status Badges**: Use rounded pill badges with subtle backgrounds (`badge bg-success-subtle text-success border border-success-subtle`).
4. **Action Column**: Right-aligned quick action buttons with accessible tooltips and labels.
5. **Empty State**: Friendly illustration/icon + clear explanation + Primary CTA button.

#### Example Table Header & Filter Bar:
```html
<div class="card border-0 shadow-sm rounded-3">
  <div class="card-header bg-transparent border-0 pt-4 px-4 pb-2 d-flex flex-wrap justify-content-between align-items-center gap-3">
    <div>
      <h5 class="fw-bold mb-1">Lançamentos Recentes</h5>
      <p class="text-muted small mb-0">Gerencie todas as despesas e receitas cadastradas.</p>
    </div>
    <div class="d-flex align-items-center gap-2">
      <div class="input-group input-group-sm" style="max-width: 260px;">
        <span class="input-group-text bg-light border-end-0"><i class="bi bi-search"></i></span>
        <input type="text" class="form-control bg-light border-start-0" placeholder="Buscar lançamentos...">
      </div>
      <button class="btn btn-sm btn-outline-secondary d-flex align-items-center gap-1">
        <i class="bi bi-funnel"></i> Filtros
      </button>
      <button class="btn btn-sm btn-primary d-flex align-items-center gap-1 shadow-sm">
        <i class="bi bi-plus-lg"></i> Novo Lançamento
      </button>
    </div>
  </div>
  
  <div class="table-responsive">
    <table class="table table-hover align-middle mb-0">
      <thead class="table-light border-bottom">
        <tr>
          <th class="ps-4">Descrição</th>
          <th>Categoria</th>
          <th>Data</th>
          <th class="text-end">Valor</th>
          <th class="text-center">Status</th>
          <th class="text-end pe-4">Ações</th>
        </tr>
      </thead>
      <tbody>
        <!-- Data rows -->
      </tbody>
    </table>
  </div>
</div>
```

---

## 3. Micro-Interactions & CSS Enhancements

```css
/* Card hover lift effect */
.hover-lift {
  transition: transform 0.2s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}
.hover-lift:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.08), 0 8px 10px -6px rgba(0, 0, 0, 0.04) !important;
}

/* Glassmorphism utility */
.glass-panel {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

[data-bs-theme="dark"] .glass-panel {
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.08);
}
```

---

## 4. Design Checklist for Dashboard Screens
- [ ] Responsive on 375px (Mobile), 768px (Tablet), and 1440px (Desktop).
- [ ] Loading skeleton states for tables and KPI cards during async data fetch.
- [ ] Clear empty state when lists/tables contain 0 items.
- [ ] Form validations display inline with clear error messages.
- [ ] Numbers and financial values formatted with currency symbol and monospace font.
- [ ] Focus rings clearly visible on all interactive elements.
