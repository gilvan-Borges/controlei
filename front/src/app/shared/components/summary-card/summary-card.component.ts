import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-summary-card',
  standalone: false,
  template: `
    <div class="summary-card stat-widget hover-lift" [class]="'variant-' + variant">
      @if (icon) {
        <div class="stat-icon-box" [ngClass]="iconClass">
          <i class="bi" [ngClass]="icon" aria-hidden="true"></i>
        </div>
      }
      <span class="stat-label">{{ label }}</span>
      <div class="stat-value font-mono" [class]="colorClass">{{ prefix }}{{ formattedValue }}</div>
      @if (trendText) {
        <div class="stat-subtext">
          <i class="bi" [ngClass]="trendIcon"></i>
          <span>{{ trendText }}</span>
        </div>
      }
    </div>
  `,
  styles: [`
    .summary-card {
      height: 100%;
      cursor: default;
    }

    .icon-primary { background: rgba(var(--c-primary-rgb), 0.15); color: var(--c-primary); }
    .icon-success { background: rgba(var(--c-success-rgb), 0.15); color: var(--c-success); }
    .icon-danger { background: rgba(var(--c-danger-rgb), 0.15); color: var(--c-danger); }
    .icon-warning { background: rgba(var(--c-warning-rgb), 0.15); color: var(--c-warning); }
    .icon-info { background: rgba(var(--c-info-rgb), 0.15); color: var(--c-info); }
    .icon-muted { background: var(--c-surface-elevated); color: var(--c-text-muted); }
  `]
})
export class SummaryCardComponent {
  @Input() label = '';
  @Input() value = 0;
  @Input() prefix = 'R$ ';
  @Input() icon = '';
  @Input() trendText = '';
  @Input() trendIcon = 'bi-arrow-up-right';
  @Input() variant: 'primary' | 'success' | 'danger' | 'warning' | 'info' | 'muted' = 'muted';

  get colorClass(): string {
    const map: Record<string, string> = {
      primary: 'text-primary',
      success: 'text-success',
      danger: 'text-danger',
      warning: 'text-warning',
      info: 'text-info',
      muted: ''
    };
    return map[this.variant] || '';
  }

  get iconClass(): string {
    return 'icon-' + (this.variant || 'muted');
  }

  get formattedValue(): string {
    return this.value.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }
}
