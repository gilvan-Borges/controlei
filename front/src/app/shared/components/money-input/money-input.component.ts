import { Component, forwardRef, Input } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'app-money-input',
  standalone: false,
  template: `
    <div class="input-group">
      <span class="input-group-text">R$</span>
      <input
        type="text"
        class="form-control"
        [placeholder]="placeholder"
        [value]="displayValue"
        (input)="onInput($event)"
        (blur)="onTouched()"
        [disabled]="disabled"
        [class.is-invalid]="invalid"
        inputmode="decimal"
      />
    </div>
  `,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => MoneyInputComponent),
      multi: true
    }
  ]
})
export class MoneyInputComponent implements ControlValueAccessor {
  @Input() placeholder = '0,00';
  @Input() disabled = false;
  @Input() invalid = false;

  displayValue = '';
  private numericValue = 0;

  private onChange: (value: number) => void = () => {};
  onTouched: () => void = () => {};

  writeValue(value: number): void {
    this.numericValue = value || 0;
    this.displayValue = this.formatDisplay(this.numericValue);
  }

  registerOnChange(fn: (value: number) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  onInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const raw = input.value.replace(/\D/g, '');
    const num = raw ? parseInt(raw, 10) / 100 : 0;
    this.numericValue = num;
    this.displayValue = this.formatDisplay(num);
    input.value = this.displayValue;
    this.onChange(num);
  }

  private formatDisplay(value: number): string {
    return value.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }
}
