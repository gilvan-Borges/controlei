import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-loading',
  standalone: false,
  template: `
    <div class="loading-state-box animate-in">
      <div class="spinner-border spinner-custom" [class]="'text-' + color" role="status">
        <span class="visually-hidden">Carregando...</span>
      </div>
      @if (message) {
        <p class="loading-text">{{ message }}</p>
      }
    </div>
  `,
  styles: [`
    .loading-state-box {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 1rem;
      min-height: 200px;
      padding: 2rem;
    }

    .spinner-custom {
      width: 2.5rem;
      height: 2.5rem;
      border-width: 3px;
    }

    .loading-text {
      margin: 0;
      font-size: 0.88rem;
      font-weight: 600;
      color: var(--c-text-muted);
    }
  `]
})
export class LoadingComponent {
  @Input() message = 'Carregando informações...';
  @Input() color = 'primary';
}
