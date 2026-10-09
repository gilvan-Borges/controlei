import { ChangeDetectorRef } from '@angular/core';
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
        {
          provide: AssistantService,
          useValue: {
            ask, confirm, cancel, getSettings, updateSettings,
            getVoiceSettings: () => of({ enabled: false, canManage: true, voiceAvailable: false, assistantEnabled: true }),
            updateVoiceSettings: vi.fn(),
            askByVoice: vi.fn()
          }
        },
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

/** MediaRecorder falso: grava "na hora" um WebM minimo quando para. */
class FakeMediaRecorder {
  static isTypeSupported = (t: string) => t.startsWith('audio/webm');
  state: 'inactive' | 'recording' = 'inactive';
  mimeType: string;
  ondataavailable: ((e: { data: Blob }) => void) | null = null;
  onstop: (() => void) | null = null;
  constructor(_stream: unknown, options?: { mimeType?: string }) {
    this.mimeType = options?.mimeType ?? 'audio/webm';
  }
  start(): void {
    this.state = 'recording';
  }
  stop(): void {
    this.state = 'inactive';
    this.ondataavailable?.({ data: new Blob([new Uint8Array([0x1a, 0x45, 0xdf, 0xa3])], { type: this.mimeType }) });
    this.onstop?.();
  }
}

describe('AssistantWidgetComponent: voz', () => {
  let fixture: ComponentFixture<AssistantWidgetComponent>;
  let component: AssistantWidgetComponent;
  const askByVoice = vi.fn();
  const updateVoiceSettings = vi.fn();
  const getVoiceSettings = vi.fn();
  const stopTrack = vi.fn();
  const getUserMedia = vi.fn();
  let now = 0;

  /** Redesenha: os testes rodam sem zone.js, entao marcamos o componente como a zona faria. */
  const render = () => {
    fixture.componentRef.injector.get(ChangeDetectorRef).markForCheck();
    fixture.detectChanges();
  };
  const el = (): HTMLElement => fixture.nativeElement;
  const mic = () => el().querySelector<HTMLButtonElement>('button.mic');

  async function create(voice: { enabled: boolean; voiceAvailable?: boolean; canManage?: boolean }) {
    getVoiceSettings.mockReturnValue(
      of({ enabled: voice.enabled, canManage: voice.canManage ?? true, voiceAvailable: voice.voiceAvailable ?? true, assistantEnabled: true })
    );
    await TestBed.configureTestingModule({
      declarations: [AssistantWidgetComponent],
      providers: [
        {
          provide: AssistantService,
          useValue: {
            ask: vi.fn(), confirm: vi.fn(), cancel: vi.fn(), updateSettings: vi.fn(),
            getSettings: () => of({ enabled: true, canManage: true, aiAvailable: true }),
            getVoiceSettings, updateVoiceSettings, askByVoice
          }
        },
        { provide: Router, useValue: { url: '/app/dashboard', routeReuseStrategy: { shouldReuseRoute: () => true }, onSameUrlNavigation: 'ignore', navigateByUrl: () => Promise.resolve(true) } }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(AssistantWidgetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    component.toggle();
    render();
  }

  beforeEach(() => {
    askByVoice.mockReset();
    updateVoiceSettings.mockReset();
    getVoiceSettings.mockReset();
    stopTrack.mockReset();
    getUserMedia.mockReset();
    getUserMedia.mockResolvedValue({ getTracks: () => [{ stop: stopTrack }] });
    now = 1_000;
    vi.spyOn(Date, 'now').mockImplementation(() => now);
    vi.stubGlobal('MediaRecorder', FakeMediaRecorder);
    Object.defineProperty(navigator, 'mediaDevices', { value: { getUserMedia }, configurable: true });
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
    Object.defineProperty(navigator, 'mediaDevices', { value: undefined, configurable: true });
  });

  it('mostra o microfone acessivel so quando a voz da familia esta ligada', async () => {
    await create({ enabled: true });

    expect(mic()).not.toBeNull();
    expect(mic()!.getAttribute('aria-label')).toBe('Perguntar por voz');
    expect(mic()!.getAttribute('aria-pressed')).toBe('false');
  });

  it('esconde o microfone com a voz desligada ou sem provedor no servidor', async () => {
    await create({ enabled: false, voiceAvailable: false });

    expect(mic()).toBeNull();
    expect(el().querySelector('.voice-banner')).toBeNull();
  });

  it('grava, envia e mostra a transcricao, a resposta e o player', async () => {
    await create({ enabled: true });
    askByVoice.mockReturnValue(
      of({
        transcript: 'quanto gastei?',
        answer: 'R$ 1.200,00 este mês.',
        ai: true,
        actions: [{ id: 'a1', summary: 'Lançar R$ 50,00' }],
        audio: { mimeType: 'audio/mpeg', base64: 'SUQz' }
      })
    );

    await component.startRecording();
    render();
    expect(component.micState).toBe('recording');
    expect(mic()!.getAttribute('aria-pressed')).toBe('true');
    expect(mic()!.getAttribute('aria-label')).toBe('Parar e enviar a pergunta gravada');
    expect(el().textContent).toContain('Gravando');

    now += 2_500;
    component.toggleRecording();
    render();

    const [blob, duration] = askByVoice.mock.calls[0];
    expect(blob).toBeInstanceOf(Blob);
    expect(blob.type).toContain('audio/webm');
    expect(duration).toBe(2_500);
    expect(stopTrack).toHaveBeenCalled();
    expect(component.micState).toBe('idle');
    const user = component.messages.at(-2)!;
    expect(user).toMatchObject({ from: 'user', text: 'quanto gastei?', voice: true });
    expect(component.messages.at(-1)!.actions?.[0].state).toBe('pending');
    const player = el().querySelector<HTMLAudioElement>('audio.voice-player');
    expect(player).not.toBeNull();
    expect(player!.getAttribute('src')).toBe('data:audio/mpeg;base64,SUQz');
    expect(player!.autoplay).toBe(true);
  });

  it('sem audio na resposta (falha da sintese) mostra so o texto', async () => {
    await create({ enabled: true });
    askByVoice.mockReturnValue(of({ transcript: 'oi', answer: 'Olá!', ai: true, actions: [], audio: null }));

    await component.startRecording();
    now += 1_000;
    component.stopRecording();
    render();

    expect(component.messages.at(-1)!.text).toBe('Olá!');
    expect(el().querySelector('audio.voice-player')).toBeNull();
  });

  it('mostra a mensagem do servidor quando a voz falha (ex.: 422)', async () => {
    await create({ enabled: true });
    askByVoice.mockReturnValue(throwError(() => ({ status: 422, error: { message: 'Não ouvi nenhuma pergunta no áudio.' } })));

    await component.startRecording();
    now += 1_000;
    component.stopRecording();

    expect(component.messages.at(-1)!.text).toBe('Não ouvi nenhuma pergunta no áudio.');
    expect(component.micState).toBe('idle');
    expect(component.loading).toBe(false);
  });

  it('sem permissao de microfone, esconde o botao e explica, sem quebrar o texto', async () => {
    await create({ enabled: true });
    getUserMedia.mockRejectedValue(new DOMException('negado', 'NotAllowedError'));

    await component.startRecording();
    render();

    expect(component.micDenied).toBe(true);
    expect(mic()).toBeNull();
    expect(el().querySelector('.voice-note')?.textContent).toContain('Microfone bloqueado');
    expect(el().querySelector('#assistant-input')).not.toBeNull();
  });

  it('o responsavel so liga a voz depois de aceitar o aviso do provedor externo', async () => {
    await create({ enabled: false });
    updateVoiceSettings.mockReturnValue(of({ enabled: true, canManage: true, voiceAvailable: true, assistantEnabled: true }));

    el().querySelector<HTMLButtonElement>('.voice-banner .primary')!.click();
    render();
    expect(el().querySelector('.voice-banner')!.textContent).toContain('provedor externo');
    component.setVoiceEnabled(true);
    expect(updateVoiceSettings).not.toHaveBeenCalled();

    component.voiceAcknowledged = true;
    component.setVoiceEnabled(true);
    render();

    expect(updateVoiceSettings).toHaveBeenCalledWith(true, true);
    expect(mic()).not.toBeNull();
  });
});
