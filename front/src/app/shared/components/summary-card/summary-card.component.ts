import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-summary-card',
  standalone: false,
  template: `
    <div class="summary-card h-100" [class]="'variant-' + variant">
      <div class="summary-orb"></div>
      <div class="summary-content">
        <small>{{ label }}</small>
        <h5 [class]="colorClass">{{ prefix }}{{ formattedValue }}</h5>
      </div>
    </div>
  `,
  styles: [`
    .summary-card {
      position: relative;
      min-height: 132px;
      overflow: hidden;
      border: 1px solid rgba(255, 255, 255, 0.13);
      border-radius: 24px;
      background:
        linear-gradient(145deg, rgba(255, 255, 255, 0.09), rgba(255, 255, 255, 0.035)),
        rgba(8, 13, 30, 0.7);
      box-shadow: 0 18px 54px rgba(0, 0, 0, 0.28);
      backdrop-filter: blur(18px);
      transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
    }

    .summary-card:hover {
      transform: translateY(-3px);
      border-color: rgba(var(--controlei-secondary-rgb), 0.36);
      box-shadow: 0 24px 70px rgba(0, 0, 0, 0.36);
    }

    .summary-orb {
      position: absolute;
      top: -2rem;
      right: -2rem;
      width: 6rem;
      height: 6rem;
      border-radius: 999px;
      background: rgba(var(--controlei-secondary-rgb), 0.18);
      filter: blur(2px);
    }

    .variant-success .summary-orb {
      background: rgba(var(--controlei-success-rgb), 0.22);
    }

    .variant-danger .summary-orb {
      background: rgba(var(--controlei-danger-rgb), 0.2);
    }

    .variant-warning .summary-orb {
      background: rgba(var(--controlei-warning-rgb), 0.2);
    }

    .summary-content {
      position: relative;
      display: grid;
      align-content: end;
      height: 100%;
      min-height: 132px;
      padding: 1rem;
    }

    small {
      color: var(--controlei-muted);
      font-size: 0.74rem;
      font-weight: 900;
      letter-spacing: 0.08em;
      text-transform: uppercase;
    }

    h5 {
      margin: 0.35rem 0 0;
      font-size: clamp(1.15rem, 2vw, 1.55rem);
      font-weight: 950;
      letter-spacing: 0;
    }
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
