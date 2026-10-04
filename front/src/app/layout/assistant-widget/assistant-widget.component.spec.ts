import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { AssistantWidgetComponent } from './assistant-widget.component';
import { AssistantService } from '../../core/services/assistant.service';

describe('AssistantWidgetComponent', () => {
  let fixture: ComponentFixture<AssistantWidgetComponent>;
  let component: AssistantWidgetComponent;
  const ask = vi.fn();

  beforeEach(async () => {
    ask.mockReset();
    await TestBed.configureTestingModule({
      declarations: [AssistantWidgetComponent],
      providers: [{ provide: AssistantService, useValue: { ask } }]
    }).compileComponents();
    fixture = TestBed.createComponent(AssistantWidgetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('starts closed with a labelled floating button', () => {
    const fab: HTMLButtonElement = fixture.nativeElement.querySelector('.assistant-fab');
    expect(fab.getAttribute('aria-label')).toBe('Abrir assistente de ajuda');
    expect(fixture.nativeElement.querySelector('.assistant-panel')).toBeNull();
  });

  it('opens the panel as a dialog and closes on Escape', async () => {
    fixture.nativeElement.querySelector('.assistant-fab').click();
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('[role="dialog"]')).not.toBeNull();

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('.assistant-panel')).toBeNull();
  });

  it('sends the question and shows the answer', () => {
    ask.mockReturnValue(of({ answer: 'Use Transações.', ai: false }));
    component.send('  como lançar?  ');

    expect(ask).toHaveBeenCalledWith('como lançar?');
    expect(component.messages.at(-1)).toEqual({ from: 'assistant', text: 'Use Transações.', ai: false });
    expect(component.loading).toBe(false);
  });

  it('ignores blank questions', () => {
    component.send('   ');
    expect(ask).not.toHaveBeenCalled();
  });

  it('explains rate limiting instead of failing silently', () => {
    ask.mockReturnValue(throwError(() => ({ status: 429 })));
    component.send('oi');
    expect(component.messages.at(-1)?.text).toContain('Muitas perguntas');
  });

  it('shows a generic message on any other error', () => {
    ask.mockReturnValue(throwError(() => ({ status: 500 })));
    component.send('oi');
    expect(component.messages.at(-1)?.text).toContain('Não consegui responder');
  });
});
