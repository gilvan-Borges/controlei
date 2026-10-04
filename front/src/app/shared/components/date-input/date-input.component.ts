import { Component, forwardRef, Input } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'app-date-input',
  standalone: false,
  template: `
    <input
      type="date"
      class="form-control"
      [value]="value"
      (change)="onChange($event)"
      (blur)="onTouched()"
      [disabled]="disabled"
      [class.is-invalid]="invalid"
      [max]="max"
      [min]="min"
    />
  `,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => DateInputComponent),
      multi: true
    }
  ]
})
export class DateInputComponent implements ControlValueAccessor {
  @Input() disabled = false;
  @Input() invalid = false;
  @Input() min = '';
  @Input() max = '';

  value = '';

  private onChangeFn: (value: string) => void = () => {};
  onTouched: () => void = () => {};

  writeValue(value: string): void {
    this.value = value || '';
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChangeFn = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  onChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.value = input.value;
    this.onChangeFn(this.value);
  }
}
