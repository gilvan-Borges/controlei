import { Component, EventEmitter, Input, Output } from '@angular/core';

export interface SelectOption {
  value: string;
  label: string;
}

@Component({
  selector: 'app-user-selector',
  standalone: false,
  template: `
    <select
      class="form-select"
      [value]="selectedUserId"
      (change)="onSelect($event)"
      [disabled]="disabled"
    >
      @if (placeholder) {
        <option value="">{{ placeholder }}</option>
      }
      @for (user of users; track user.value) {
        <option [value]="user.value">{{ user.label }}</option>
      }
    </select>
  `
})
export class UserSelectorComponent {
  @Input() users: SelectOption[] = [];
  @Input() selectedUserId = '';
  @Input() placeholder = 'Todos os membros';
  @Input() disabled = false;
  @Output() userChange = new EventEmitter<string>();

  onSelect(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.userChange.emit(select.value);
  }
}
