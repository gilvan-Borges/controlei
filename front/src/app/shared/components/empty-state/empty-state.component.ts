import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-empty-state',
  standalone: false,
  template: `
    <div class="empty-state-box animate-in">
      <div class="empty-icon-circle">
        <i class="bi" [ngClass]="icon" aria-hidden="true"></i>
      </div>
      <h6 class="empty-title">{{ message }}</h6>
      @if (description) {
        <p class="empty-desc">{{ description }}</p>
      }
    </div>
  `,
  styles: [`
    .empty-state-box {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 3rem 1.5rem;
      text-align: center;
      background: var(--c-surface-card);
      border: 1px dashed var(--c-border);
      border-radius: var(--c-radius);
      margin: 1rem 0;
    }

    .empty-icon-circle {
      width: 60px;
      height: 60px;
      border-radius: 16px;
      background: rgba(var(--c-primary-rgb), 0.1);
      color: var(--c-primary);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.75rem;
      margin-bottom: 1rem;
    }

    .empty-title {
      font-size: 1rem;
      font-weight: 700;
      color: var(--c-text);
      margin: 0 0 0.25rem;
    }

    .empty-desc {
      font-size: 0.85rem;
      color: var(--c-text-muted);
      max-width: 380px;
      margin: 0;
    }
  `]
})
export class EmptyStateComponent {
  @Input() icon = 'bi-inbox';
  @Input() message = 'Nenhum registro encontrado.';
  @Input() description = '';
}
