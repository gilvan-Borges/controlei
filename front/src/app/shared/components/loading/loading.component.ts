import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-loading',
  standalone: false,
  template: `
    <div class="loading-state">
      <div class="spinner-border loader-ring" [class]="'text-' + color" role="status">
        <span class="visually-hidden">Carregando...</span>
      </div>
      @if (message) {
        <p>{{ message }}</p>
      }
    </div>
  `,
  styles: [`
    .loading-state {
      display: grid;
      place-items: center;
      gap: 0.9rem;
      min-height: 220px;
      color: var(--controlei-muted);
    }

    .loader-ring {
      width: 3rem;
      height: 3rem;
      border: 3px solid rgba(255, 255, 255, 0.12);
      border-top-color: var(--controlei-secondary);
      border-radius: 999px;
      box-shadow: 0 0 30px rgba(var(--controlei-secondary-rgb), 0.28);
      animation: spin 0.85s linear infinite;
    }

    p {
      margin: 0;
      font-weight: 800;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }
  `]
})
export class LoadingComponent {
  @Input() message = 'Carregando...';
  @Input() color = 'primary';
}
