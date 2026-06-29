import { Component, EventEmitter, Input, Output } from '@angular/core';
import { SelectOption } from '../user-selector/user-selector.component';

@Component({
  selector: 'app-category-selector',
  standalone: false,
  template: `
    <select
      class="form-select"
      [value]="selectedCategoryId"
      (change)="onSelect($event)"
      [disabled]="disabled"
    >
      @if (placeholder) {
        <option value="">{{ placeholder }}</option>
      }
      @for (cat of categories; track cat.value) {
        <option [value]="cat.value">{{ cat.label }}</option>
      }
    </select>
  `
})
export class CategorySelectorComponent {
  @Input() categories: SelectOption[] = [];
  @Input() selectedCategoryId = '';
  @Input() placeholder = 'Todas as categorias';
  @Input() disabled = false;
  @Output() categoryChange = new EventEmitter<string>();

  onSelect(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.categoryChange.emit(select.value);
  }
}
