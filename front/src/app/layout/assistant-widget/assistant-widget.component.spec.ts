import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { Router } from '@angular/router';
import { AssistantWidgetComponent } from './assistant-widget.component';
import { AssistantService } from '../../core/services/assistant.service';

describe('AssistantWidgetComponent', () => {
  let fixture: ComponentFixture<AssistantWidgetComponent>;
  let component: AssistantWidgetComponent;
  const ask = vi.fn();
  const confirm = vi.fn();
  const cancel = vi.fn();
  const getSettings = vi.fn();
  const updateSettings = vi.fn();

  beforeEach(async () => {
    ask.mockReset();
    confirm.mockReset();
    cancel.mockReset();
    getSettings.mockReset();
    updateSettings.mockReset();
    getSettings.mockReturnValue(of({ enabled: true, canManage: true, aiAvailable: true }));
    await TestBed.configureTestingModule({
      declarations: [AssistantWidgetComponent],
      providers: [
        { provide: AssistantService, useValue: { ask, confirm, cancel, getSettings, updateSettings } },
        { provide: Router, useValue: { url: '/app/dashboard', routeReuseStrategy: { shouldReuseRoute: () => true }, onSameUrlNavigation: 'ignore', navigateByUrl: () => Promise.resolve(true) } }
      ]
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
    ask.mockReturnValue(of({ answer: 'Use Transações.', ai: false, actions: [] }));
    component.send('  como lançar?  ');

    expect(ask).toHaveBeenCalledWith('como lançar?', []);
    expect(component.messages.at(-1)).toEqual({ from: 'assistant', text: 'Use Transações.', ai: false, actions: [] });
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

  it('shows a prepared action as a card and runs it only after Confirmar', () => {
    ask.mockReturnValue(of({ answer: 'Preparei.', ai: true, actions: [{ id: 'a1', summary: 'Lançar despesa de R$ 50,00' }] }));
    confirm.mockReturnValue(of({ message: 'Despesa lançada.' }));
    component.send('gastei 50');

    const action = component.messages.at(-1)!.actions![0];
    expect(action.state).toBe('pending');
    expect(confirm).not.toHaveBeenCalled();

    component.confirm(action);

    expect(confirm).toHaveBeenCalledWith('a1');
    expect(action.state).toBe('done');
    expect(component.messages.at(-1)?.text).toBe('Despesa lançada.');
  });

  it('cancels a prepared action without executing it', () => {
    ask.mockReturnValue(of({ answer: 'Preparei.', ai: true, actions: [{ id: 'a2', summary: 'Criar meta' }] }));
    cancel.mockReturnValue(of(undefined));
    component.send('crie uma meta');

    const action = component.messages.at(-1)!.actions![0];
    component.cancel(action);

    expect(cancel).toHaveBeenCalledWith('a2');
    expect(confirm).not.toHaveBeenCalled();
    expect(action.state).toBe('canceled');
  });

  it('never executes the same action twice', () => {
    ask.mockReturnValue(of({ answer: 'Preparei.', ai: true, actions: [{ id: 'a3', summary: 'x' }] }));
    confirm.mockReturnValue(of({ message: 'ok' }));
    component.send('faça');
    const action = component.messages.at(-1)!.actions![0];

    component.confirm(action);
    component.confirm(action);

    expect(confirm).toHaveBeenCalledTimes(1);
  });

  it('sends only user and assistant text as history, never the action cards', () => {
    ask.mockReturnValue(of({ answer: 'Preparei.', ai: true, actions: [{ id: 'a4', summary: 'x' }] }));
    component.send('primeira');
    ask.mockReturnValue(of({ answer: 'ok', ai: true, actions: [] }));
    component.send('segunda');

    const history = ask.mock.calls[1][1];
    expect(history).toEqual([
      { role: 'user', text: 'primeira' },
      { role: 'assistant', text: 'Preparei.' }
    ]);
  });

  it('offers the responsible a consent panel and only enables after the acknowledgement', () => {
    getSettings.mockReturnValue(of({ enabled: false, canManage: true, aiAvailable: true }));
    updateSettings.mockReturnValue(of({ enabled: true, canManage: true, aiAvailable: true }));
    component.loadSettings();

    component.openConsent();
    component.setEnabled(true);
    expect(updateSettings).not.toHaveBeenCalled();

    component.acknowledged = true;
    component.setEnabled(true);

    expect(updateSettings).toHaveBeenCalledWith(true, true);
    expect(component.settings?.enabled).toBe(true);
    expect(component.consentOpen).toBe(false);
  });

  it('shows members who cannot manage a hint instead of the activation button', async () => {
    getSettings.mockReturnValue(of({ enabled: false, canManage: false, aiAvailable: true }));
    fixture.nativeElement.querySelector('.assistant-fab').click();
    await fixture.whenStable();

    const banner: HTMLElement = fixture.nativeElement.querySelector('.assistant-banner');
    expect(banner.textContent).toContain('Peça ao responsável');
    expect(banner.querySelector('button')).toBeNull();
  });

  it('marks a destructive action and labels its button Excluir', async () => {
    ask.mockReturnValue(of({ answer: 'Preparei.', ai: true, actions: [{ id: 'd1', summary: 'EXCLUIR meta X', destructive: true }] }));
    fixture.nativeElement.querySelector('.assistant-fab').click();
    await fixture.whenStable();
    component.send('exclua a meta X');
    await fixture.whenStable();

    const card: HTMLElement = fixture.nativeElement.querySelector('.assistant-action');
    expect(card.classList.contains('destructive')).toBe(true);
    expect(card.querySelector('.confirm')?.textContent?.trim()).toBe('Excluir');
  });
});
