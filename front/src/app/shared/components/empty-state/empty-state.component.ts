import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-empty-state',
  standalone: false,
  template: `
    <div class="empty-state">
      <div class="empty-icon">
        <i class="bi" [ngClass]="icon"></i>
      </div>
      <p>{{ message }}</p>
      @if (description) {
        <small>{{ description }}</small>
      }
    </div>
  `,
  styles: [`
    .empty-state {
      display: grid;
      place-items: center;
      gap: 0.75rem;
      padding: 2.5rem 1rem;
      text-align: center;
      color: var(--controlei-muted);
    }

    .empty-icon {
      display: grid;
      place-items: center;
      width: 4rem;
      height: 4rem;
      color: var(--controlei-secondary);
      border: 1px solid rgba(var(--controlei-secondary-rgb), 0.26);
      border-radius: 1.35rem;
      background: rgba(var(--controlei-secondary-rgb), 0.08);
      box-shadow: 0 18px 44px rgba(var(--controlei-secondary-rgb), 0.12);
    }

    i {
      font-size: 1.9rem;
    }

    p {
      margin: 0;
      color: var(--controlei-text);
      font-weight: 900;
    }

    small {
      max-width: 320px;
    }
  `]
})
export class EmptyStateComponent {
  @Input() icon = 'bi-inbox';
  @Input() message = 'Nenhum dado encontrado.';
  @Input() description = '';
}
