import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-error-message',
  standalone: false,
  template: `
    <div class="alert alert-danger d-flex align-items-center" role="alert">
      <i class="bi bi-exclamation-triangle-fill me-2"></i>
      <div>{{ message }}</div>
    </div>
  `
})
export class ErrorMessageComponent {
  @Input() message = 'Ocorreu um erro. Tente novamente.';
}
