import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-empty-state',
  standalone: false,
  template: `
    <div class="text-center py-5">
      <i class="bi" [ngClass]="icon" style="font-size: 2.5rem; color: #ccc;"></i>
      <p class="text-muted mt-3 mb-1">{{ message }}</p>
      @if (description) {
        <small class="text-muted">{{ description }}</small>
      }
    </div>
  `
})
export class EmptyStateComponent {
  @Input() icon = 'bi-inbox';
  @Input() message = 'Nenhum dado encontrado.';
  @Input() description = '';
}
