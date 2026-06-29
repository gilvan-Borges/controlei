import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-action-button',
  standalone: false,
  template: `
    <button
      [type]="type"
      [class]="'btn btn-' + variant + ' action-button ' + sizeClass + ' ' + extraClass"
      [disabled]="disabled || loading"
    >
      @if (loading) {
        <span class="spinner-border spinner-border-sm me-1" role="status"></span>
      }
      @if (icon && !loading) {
        <i class="bi" [ngClass]="icon" [class.me-1]="label"></i>
      }
      {{ label }}
    </button>
  `,
  styles: [`
    .action-button {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 0.35rem;
      letter-spacing: 0.01em;
    }
  `]
})
export class ActionButtonComponent {
  @Input() label = '';
  @Input() icon = '';
  @Input() variant: 'primary' | 'success' | 'danger' | 'warning' | 'outline-primary' | 'outline-danger' = 'primary';
  @Input() size: 'sm' | 'md' | 'lg' = 'md';
  @Input() type: 'button' | 'submit' = 'button';
  @Input() disabled = false;
  @Input() loading = false;
  @Input() extraClass = '';

  get sizeClass(): string {
    return this.size === 'sm' ? 'btn-sm' : this.size === 'lg' ? 'btn-lg' : '';
  }
}
