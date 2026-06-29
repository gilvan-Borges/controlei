import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-summary-card',
  standalone: false,
  template: `
    <div class="card h-100">
      <div class="card-body text-center py-3">
        <small class="text-muted d-block mb-1">{{ label }}</small>
        <h5 class="mb-0" [class]="colorClass">{{ prefix }}{{ formattedValue }}</h5>
      </div>
    </div>
  `
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
