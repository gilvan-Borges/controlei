import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-error-message',
  standalone: false,
  template: `
    <div class="alert alert-danger d-flex align-items-center error-message" role="alert">
      <i class="bi bi-exclamation-triangle-fill me-2"></i>
      <div>{{ message }}</div>
    </div>
  `,
  styles: [`
    .error-message {
      font-weight: 800;
      box-shadow: 0 18px 44px rgba(var(--controlei-danger-rgb), 0.12);
    }
  `]
})
export class ErrorMessageComponent {
  @Input() message = 'Ocorreu um erro. Tente novamente.';
}
