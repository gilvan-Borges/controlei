import { TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { MoneyInputComponent } from './shared/components/money-input/money-input.component';
import { SummaryCardComponent } from './shared/components/summary-card/summary-card.component';
import { EmptyStateComponent } from './shared/components/empty-state/empty-state.component';
import { LoadingComponent } from './shared/components/loading/loading.component';
import { ErrorMessageComponent } from './shared/components/error-message/error-message.component';
import { ConfirmModalComponent } from './shared/components/confirm-modal/confirm-modal.component';
import { ActionButtonComponent } from './shared/components/action-button/action-button.component';
import { UserSelectorComponent } from './shared/components/user-selector/user-selector.component';
import { CategorySelectorComponent } from './shared/components/category-selector/category-selector.component';
import { DateInputComponent } from './shared/components/date-input/date-input.component';

describe('Shared Components', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormsModule],
      declarations: [
        MoneyInputComponent,
        SummaryCardComponent,
        EmptyStateComponent,
        LoadingComponent,
        ErrorMessageComponent,
        ConfirmModalComponent,
        ActionButtonComponent,
        UserSelectorComponent,
        CategorySelectorComponent,
        DateInputComponent
      ]
    }).compileComponents();
  });

  describe('MoneyInputComponent', () => {
    it('should create', () => {
      const fixture = TestBed.createComponent(MoneyInputComponent);
      expect(fixture.componentInstance).toBeTruthy();
    });

    it('should format value as BRL', () => {
      const fixture = TestBed.createComponent(MoneyInputComponent);
      const component = fixture.componentInstance;
      component.writeValue(1234.56);
      expect(component.displayValue).toContain('1');
      expect(component.displayValue).toContain('234');
      expect(component.displayValue).toContain('56');
    });

    it('should display R$ prefix', () => {
      const fixture = TestBed.createComponent(MoneyInputComponent);
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      expect(compiled.querySelector('.input-group-text')?.textContent).toContain('R$');
    });
  });

  describe('SummaryCardComponent', () => {
    it('should create', () => {
      const fixture = TestBed.createComponent(SummaryCardComponent);
      expect(fixture.componentInstance).toBeTruthy();
    });

    it('should display label and formatted value', () => {
      const fixture = TestBed.createComponent(SummaryCardComponent);
      fixture.componentInstance.label = 'Saldo';
      fixture.componentInstance.value = 1000;
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      expect(compiled.textContent).toContain('Saldo');
      expect(compiled.textContent).toContain('R$');
    });
  });

  describe('EmptyStateComponent', () => {
    it('should create', () => {
      const fixture = TestBed.createComponent(EmptyStateComponent);
      expect(fixture.componentInstance).toBeTruthy();
    });

    it('should display message', () => {
      const fixture = TestBed.createComponent(EmptyStateComponent);
      fixture.componentInstance.message = 'Nenhum item.';
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      expect(compiled.textContent).toContain('Nenhum item.');
    });
  });

  describe('LoadingComponent', () => {
    it('should create', () => {
      const fixture = TestBed.createComponent(LoadingComponent);
      expect(fixture.componentInstance).toBeTruthy();
    });

    it('should display spinner', () => {
      const fixture = TestBed.createComponent(LoadingComponent);
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      expect(compiled.querySelector('.spinner-border')).toBeTruthy();
    });
  });

  describe('ErrorMessageComponent', () => {
    it('should create', () => {
      const fixture = TestBed.createComponent(ErrorMessageComponent);
      expect(fixture.componentInstance).toBeTruthy();
    });

    it('should display error message', () => {
      const fixture = TestBed.createComponent(ErrorMessageComponent);
      fixture.componentInstance.message = 'Erro de teste.';
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      expect(compiled.textContent).toContain('Erro de teste.');
    });
  });

  describe('ConfirmModalComponent', () => {
    it('should create', () => {
      const fixture = TestBed.createComponent(ConfirmModalComponent);
      expect(fixture.componentInstance).toBeTruthy();
    });

    it('should emit confirmed event', () => {
      const fixture = TestBed.createComponent(ConfirmModalComponent);
      const component = fixture.componentInstance;
      component.visible = true;
      fixture.detectChanges();

      let called = false;
      component.confirmed.subscribe(() => { called = true; });

      component.onConfirm();
      expect(called).toBe(true);
    });

    it('should emit cancelled event', () => {
      const fixture = TestBed.createComponent(ConfirmModalComponent);
      const component = fixture.componentInstance;
      component.visible = true;
      fixture.detectChanges();

      let called = false;
      component.cancelled.subscribe(() => { called = true; });

      component.onCancel();
      expect(called).toBe(true);
    });
  });

  describe('ActionButtonComponent', () => {
    it('should create', () => {
      const fixture = TestBed.createComponent(ActionButtonComponent);
      expect(fixture.componentInstance).toBeTruthy();
    });

    it('should show spinner when loading', () => {
      const fixture = TestBed.createComponent(ActionButtonComponent);
      fixture.componentInstance.loading = true;
      fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      expect(compiled.querySelector('.spinner-border')).toBeTruthy();
    });
  });

  describe('UserSelectorComponent', () => {
    it('should create', () => {
      const fixture = TestBed.createComponent(UserSelectorComponent);
      expect(fixture.componentInstance).toBeTruthy();
    });
  });

  describe('CategorySelectorComponent', () => {
    it('should create', () => {
      const fixture = TestBed.createComponent(CategorySelectorComponent);
      expect(fixture.componentInstance).toBeTruthy();
    });
  });

  describe('DateInputComponent', () => {
    it('should create', () => {
      const fixture = TestBed.createComponent(DateInputComponent);
      expect(fixture.componentInstance).toBeTruthy();
    });
  });
});
