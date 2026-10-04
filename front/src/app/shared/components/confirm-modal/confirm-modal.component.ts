import { Component, EventEmitter, Input, Output, TemplateRef } from '@angular/core';

@Component({
  selector: 'app-confirm-modal',
  standalone: false,
  template: `
    @if (visible) {
      <div class="modal-backdrop fade show" (click)="onCancel()"></div>
      <div class="modal d-block" tabindex="-1">
        <div class="modal-dialog modal-dialog-centered">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">{{ title }}</h5>
              <button type="button" class="btn-close" (click)="onCancel()"></button>
            </div>
            <div class="modal-body">
              <p>{{ message }}</p>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" (click)="onCancel()">
                {{ cancelLabel }}
              </button>
              <button type="button" [class]="'btn btn-' + confirmVariant" (click)="onConfirm()">
                {{ confirmLabel }}
              </button>
            </div>
          </div>
        </div>
      </div>
    }
  `
})
export class ConfirmModalComponent {
  @Input() visible = false;
  @Input() title = 'Confirmação';
  @Input() message = 'Tem certeza?';
  @Input() confirmLabel = 'Confirmar';
  @Input() cancelLabel = 'Cancelar';
  @Input() confirmVariant: 'primary' | 'danger' | 'warning' = 'primary';
  @Output() confirmed = new EventEmitter<void>();
  @Output() cancelled = new EventEmitter<void>();

  onConfirm(): void {
    this.confirmed.emit();
  }

  onCancel(): void {
    this.cancelled.emit();
  }
}
