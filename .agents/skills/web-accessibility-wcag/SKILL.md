---
name: web-accessibility-wcag
description: "Accessibility (a11y) and WCAG 2.2 AA/AAA compliance skill. Use when creating, auditing, or refactoring UI components, forms, modals, tables, dialogs, navigation, color contrast, keyboard focus, and ARIA labels to ensure full accessibility for screen readers and keyboard-only users."
---

# Web Accessibility (WCAG 2.2 AA / a11y) Skill

This skill ensures that all user interfaces comply with international web accessibility standards (WCAG 2.2 Level AA / AAA).

## When to Apply
- Building or refactoring form controls, inputs, dropdowns, custom select boxes, modals, and tabs.
- Reviewing color contrast ratios (text vs background, icons, borders).
- Implementing keyboard navigation (Tab, Enter, Escape, Arrow keys) and focus trapping in dialogs.
- Adding ARIA attributes (`aria-expanded`, `aria-controls`, `aria-live`, `aria-describedby`, `aria-label`).
- Designing screen-reader friendly tables, charts, error summaries, and dynamic status announcements.

---

## 1. Core WCAG 2.2 Principles & Checklists

### A. Perceivable
1. **Color Contrast (WCAG 1.4.3 & 1.4.11)**:
   - Normal text (< 18pt or < 14pt bold): Minimum **4.5:1** contrast ratio.
   - Large text (≥ 18pt or ≥ 14pt bold): Minimum **3:1** contrast ratio.
   - UI components & graphical objects (active borders, icons): Minimum **3:1** contrast ratio.
   - **Never rely on color alone** to convey meaning (always pair color with an icon, badge, or text label).
2. **Text Scaling & Reflow (WCAG 1.4.4 & 1.4.10)**:
   - Layout must support 200% text zoom without breaking or clipping content.
   - No horizontal scrolling at 320px width for standard responsive content.
3. **Images & Icons (WCAG 1.1.1)**:
   - Meaningful images must have descriptive `alt="Description"`.
   - Purely decorative icons must have `aria-hidden="true"`.
   - Icon-only action buttons (e.g. `<button><i class="bi bi-trash"></i></button>`) MUST have `aria-label="Excluir item"` or a hidden `.visually-hidden` span.

### B. Operable
1. **Keyboard Accessibility (WCAG 2.1.1 & 2.1.2)**:
   - Every interactive element must be reachable and operable via Keyboard (`Tab`, `Shift+Tab`, `Space`, `Enter`, `Esc`).
   - No keyboard traps. In modals, pressing `Escape` must close the modal and return focus to the trigger button.
2. **Focus Visible & Focus Order (WCAG 2.4.7 & 2.4.11)**:
   - **NEVER** use `outline: none` without providing an enhanced `:focus-visible` ring (e.g. `box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.4); outline: 2px solid #2563eb;`).
   - Focus order must follow the visual and logical reading flow.
3. **Target Size (WCAG 2.5.8)**:
   - All interactive touch targets must be at least **44×44px** (or minimum 24×24px with at least 8px surrounding clearance).

### C. Understandable
1. **Form Labels & Error Handling (WCAG 3.3.1 & 3.3.2)**:
   - Every form input MUST have an associated `<label for="inputId">`.
   - Never use placeholder text as the only label.
   - Validation errors must be explicitly linked to inputs via `aria-describedby="error-id"`, and the input must receive `aria-invalid="true"` when invalid.
2. **Predictable Behavior (WCAG 3.2)**:
   - Changing a select option or radio button must not trigger an unexpected page jump or modal open without explicit confirmation.

### D. Robust
1. **Semantic HTML5 & ARIA (WCAG 4.1.2 & 4.1.3)**:
   - Prefer native HTML (`<button>`, `<dialog>`, `<nav>`, `<main>`, `<header>`, `<table>`) over `<div>` with click handlers.
   - For live updates (toasts, count changes), use `aria-live="polite"` or `role="status"`.
   - For alert/error banners, use `role="alert"`.

---

## 2. Practical Code Recipes (Angular / Web)

### Accessible Icon Button
```html
<!-- Good: Accessible to screen readers and keyboard users -->
<button 
  type="button" 
  class="btn btn-outline-danger" 
  aria-label="Excluir despesa do aluguel"
  (click)="onDelete(item)">
  <i class="bi bi-trash" aria-hidden="true"></i>
</button>
```

### Accessible Form Field with Live Validation
```html
<div class="mb-3">
  <label for="valorDespesa" class="form-label fw-semibold">Valor da Despesa (R$)</label>
  <input 
    type="text" 
    id="valorDespesa" 
    name="valor" 
    class="form-control" 
    [class.is-invalid]="form.get('valor')?.invalid && form.get('valor')?.touched"
    [attr.aria-invalid]="form.get('valor')?.invalid && form.get('valor')?.touched"
    aria-describedby="valorDespesaHelp valorDespesaError"
    formControlName="valor" 
    placeholder="0,00">
  <div id="valorDespesaHelp" class="form-text text-muted">Informe o valor total com centavos.</div>
  @if (form.get('valor')?.invalid && form.get('valor')?.touched) {
    <div id="valorDespesaError" class="invalid-feedback" role="alert">
      Por favor, insira um valor numérico válido maior que zero.
    </div>
  }
</div>
```

### Accessible Modal Dialog & Focus Trap
```html
<div 
  class="modal show d-block" 
  tabindex="-1" 
  role="dialog" 
  aria-modal="true" 
  aria-labelledby="modalTitle"
  (keydown.escape)="closeModal()">
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="modalTitle">Confirmar Pagamento</h5>
        <button type="button" class="btn-close" aria-label="Fechar modal" (click)="closeModal()"></button>
      </div>
      <div class="modal-body">
        <p>Tem certeza de que deseja marcar este lançamento como liquidado?</p>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" (click)="closeModal()">Cancelar</button>
        <button type="button" class="btn btn-primary" (click)="confirm()">Confirmar</button>
      </div>
    </div>
  </div>
</div>
```
