import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-loading',
  standalone: false,
  template: `
    <div class="text-center py-5">
      <div class="spinner-border" [class]="'text-' + color" role="status">
        <span class="visually-hidden">Carregando...</span>
      </div>
      @if (message) {
        <p class="text-muted mt-2 mb-0">{{ message }}</p>
      }
    </div>
  `
})
export class LoadingComponent {
  @Input() message = 'Carregando...';
  @Input() color = 'primary';
}
