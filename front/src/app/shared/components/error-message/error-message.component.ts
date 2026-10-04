import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-error-message',
  standalone: false,
  template: `
    <div class="alert alert-danger d-flex align-items-center error-banner animate-in" role="alert">
      <i class="bi bi-exclamation-triangle-fill me-2 fs-5"></i>
      <div>{{ message }}</div>
    </div>
  `,
  styles: [`
    .error-banner {
      border-radius: var(--c-radius-sm);
      border: 1px solid rgba(239, 68, 68, 0.3);
      background: rgba(239, 68, 68, 0.12);
      color: var(--c-danger);
      font-weight: 600;
      font-size: 0.88rem;
      padding: 0.85rem 1.15rem;
    }
  `]
})
export class ErrorMessageComponent {
  @Input() message = 'Ocorreu um erro ao processar os dados. Tente novamente.';
}
