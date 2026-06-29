import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-summary-card',
  standalone: false,
  template: `
    <div class="summary-card glass-panel shine" [class]="'variant-' + variant">
      <small>{{ label }}</small>
      <strong [class]="colorClass">{{ prefix }}{{ formattedValue }}</strong>
    </div>
  `,
  styles: [`
    .summary-card {
      display: flex;
      flex-direction: column;
      justify-content: center;
      gap: 0.3rem;
      padding: 0.8rem 0.95rem;
      cursor: default;
    }

    small {
      color: var(--controlei-muted);
      font-size: 0.6rem;
      font-weight: 650;
      letter-spacing: 0.08em;
      text-transform: uppercase;
    }

    strong {
      font-size: 1.1rem;
      font-weight: 750;
      letter-spacing: -0.02em;
      color: var(--controlei-text);
      transition: color 0.3s ease;
    }

    .variant-success strong { color: var(--controlei-success); }
    .variant-danger strong { color: var(--controlei-danger); }
    .variant-warning strong { color: var(--controlei-warning); }
    .variant-primary strong { color: var(--controlei-primary); }
  `]
})
export class SummaryCardComponent {
  @Input() label = '';
  @Input() value = 0;
  @Input() prefix = 'R$ ';
  @Input() variant: 'primary' | 'success' | 'danger' | 'warning' | 'muted' = 'muted';

  get colorClass(): string {
    const map: Record<string, string> = {
      primary: 'text-primary',
      success: 'text-success',
      danger: 'text-danger',
      warning: 'text-warning',
      muted: 'text-muted'
    };
    return map[this.variant] || 'text-muted';
  }

  get formattedValue(): string {
    return this.value.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }
}
