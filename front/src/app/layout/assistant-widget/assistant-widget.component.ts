import { ChangeDetectorRef, Component, ElementRef, HostListener, OnDestroy, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { AssistantService, AssistantSettings, PreparedAction, Turn, VoiceSettings } from '../../core/services/assistant.service';

export interface ChatAction extends PreparedAction {
  state: 'pending' | 'working' | 'done' | 'canceled' | 'failed';
}

export interface ChatMessage {
  from: 'user' | 'assistant';
  text: string;
  ai?: boolean;
  actions?: ChatAction[];
  /** Mensagem local (não vai ao servidor nem entra no histórico enviado ao modelo). */
  local?: boolean;
  /** Mensagem da lista "o que sei fazer": mostra os exemplos tocáveis logo abaixo. */
  caps?: boolean;
  /** Pergunta feita por voz (mostra o ícone de microfone na transcrição). */
  voice?: boolean;
  /** Resposta falada (data: URI do MP3). */
  audioSrc?: string;
  /** Toca sozinha: só quando a própria pessoa acabou de gravar. */
  autoplay?: boolean;
}

/** Estado do microfone: parado, gravando ou esperando a resposta do servidor. */
export type MicState = 'idle' | 'recording' | 'processing';

/** Tipos que o servidor aceita, na ordem de preferência para gravar. */
const RECORD_TYPES = ['audio/webm;codecs=opus', 'audio/webm', 'audio/ogg;codecs=opus', 'audio/mp4'];

/** Trecho de texto da resposta: pode estar em negrito. */
export interface Part {
  text: string;
  bold: boolean;
}

/** Bloco da resposta: parágrafo ou item de lista. Evita innerHTML: o texto do modelo nunca vira HTML. */
export interface Block {
  type: 'p' | 'li';
  parts: Part[];
}

export interface Capability {
  icon: string;
  title: string;
  examples: string[];
}

/**
 * Assistente flutuante: tira dúvidas, consulta os dados da família e prepara ações. Nada é alterado sem a pessoa
 * confirmar no cartão da ação, que mostra o resumo montado pelo servidor.
 * Acessível: botão com rótulo, painel como diálogo, Esc fecha e devolve o foco, respostas anunciadas por aria-live.
 */
@Component({
  selector: 'app-assistant-widget',
  standalone: false,
  templateUrl: './assistant-widget.component.html',
  styleUrl: './assistant-widget.component.scss'
})
export class AssistantWidgetComponent implements OnDestroy {
  readonly maxLength = 500;
  /** Teto da gravação; o servidor recusa acima disso. */
  static readonly MAX_RECORDING_MS = 60_000;

  readonly capabilities: Capability[] = [
    {
      icon: 'bi-search',
      title: 'Consultar',
      examples: ['Quanto gastei este mês?', 'Como estão meus orçamentos?', 'Mostre minhas últimas transações', 'Como estão minhas metas?']
    },
    {
      icon: 'bi-plus-circle',
      title: 'Lançar',
      examples: ['Lance uma despesa de 50 reais no mercado', 'Recebi 3000 de salário hoje', 'Crie a categoria Pets']
    },
    {
      icon: 'bi-piggy-bank',
      title: 'Planejar',
      examples: ['Crie um orçamento de 800 para Alimentação', 'Crie a meta Viagem de 5000', 'Aporte 200 na meta Viagem']
    },
    {
      icon: 'bi-pencil-square',
      title: 'Corrigir',
      examples: ['Altere o valor da última despesa para 60', 'Marque a conta de luz como paga', 'Exclua o lançamento do mercado']
    }
  ];

  open = false;
  expanded = false;
  loading = false;
  settings: AssistantSettings | null = null;
  /** Já abriu o assistente alguma vez: depois disso o ícone para de chamar atenção. */
  seen = AssistantWidgetComponent.readSeen();
  /** Painel de ciência aberto: o responsável precisa aceitar antes de ligar a IA. */
  consentOpen = false;
  acknowledged = false;
  savingSettings = false;
  /** Quantos caracteres já digitados, para o contador perto do limite. */
  typed = 0;

  voiceSettings: VoiceSettings | null = null;
  voiceConsentOpen = false;
  voiceAcknowledged = false;
  micState: MicState = 'idle';
  /** Segundos gravados, para o contador ao lado do botão. */
  recordedSeconds = 0;
  /** O navegador negou o microfone: o botão some e a nota explica como liberar. */
  micDenied = false;
  /** Frase anunciada aos leitores de tela ("Gravando…", "Processando…"). */
  voiceStatus = '';
  /** O navegador tem gravação de áudio (MediaRecorder + getUserMedia, só em HTTPS ou localhost). */
  readonly micSupported =
    typeof window !== 'undefined' &&
    typeof (window as { MediaRecorder?: unknown }).MediaRecorder !== 'undefined' &&
    !!navigator.mediaDevices?.getUserMedia;

  private recorder: MediaRecorder | null = null;
  private stream: MediaStream | null = null;
  private chunks: Blob[] = [];
  private recordStart = 0;
  private recordTimer: ReturnType<typeof setTimeout> | null = null;
  private tickTimer: ReturnType<typeof setInterval> | null = null;

  /** Botão de microfone aparece só quando tudo permite: navegador, servidor, assistente e voz da família ligados. */
  get canUseVoice(): boolean {
    return (
      this.micSupported &&
      !this.micDenied &&
      !!this.settings?.enabled &&
      !!this.voiceSettings?.voiceAvailable &&
      !!this.voiceSettings?.enabled
    );
  }

  /** O responsável pode oferecer a voz: servidor tem voz, o assistente está ligado e a voz ainda não. */
  get canOfferVoice(): boolean {
    return (
      this.micSupported &&
      !!this.settings?.enabled &&
      !!this.voiceSettings?.voiceAvailable &&
      !this.voiceSettings.enabled &&
      this.voiceSettings.canManage
    );
  }

  messages: ChatMessage[] = [this.welcome()];

  /** Exemplos tocáveis: na conversa nova e logo depois de "o que sei fazer". */
  get showExamples(): boolean {
    return this.messages.length === 1 || this.messages.at(-1)?.caps === true;
  }

  @ViewChild('input') input?: ElementRef<HTMLTextAreaElement>;
  @ViewChild('list') list?: ElementRef<HTMLElement>;
  @ViewChild('fab') fab?: ElementRef<HTMLButtonElement>;

  constructor(
    private assistant: AssistantService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  toggle(): void {
    this.open = !this.open;
    if (this.open) {
      this.markSeen();
      this.loadSettings();
      setTimeout(() => this.input?.nativeElement.focus());
    }
  }

  @HostListener('document:keydown.escape')
  close(): void {
    if (this.micState === 'recording') {
      // Fechar no meio da gravação descarta o áudio: nada é enviado sem a pessoa querer
      if (this.recorder) {
        this.recorder.onstop = null;
      }
      this.stopRecording();
      this.releaseMic();
      this.micState = 'idle';
      this.voiceStatus = '';
    }
    if (this.open) {
      this.open = false;
      this.fab?.nativeElement.focus();
    }
  }

  toggleExpanded(): void {
    this.expanded = !this.expanded;
    this.scrollDown();
  }

  /** Recomeça a conversa. Ações ainda pendentes continuam valendo no servidor por 10 min, mas somem da tela. */
  reset(): void {
    this.messages = [this.welcome()];
    this.loading = false;
    this.typed = 0;
    this.input?.nativeElement.focus();
  }

  /** Mostra, sem chamar o servidor, tudo o que o assistente sabe fazer, com exemplos para tocar. */
  showCapabilities(): void {
    this.messages.push({
      from: 'assistant',
      local: true,
      caps: true,
      text:
        'Posso ajudar de quatro jeitos. Toque em um exemplo ou escreva do seu jeito:\n' +
        '- **Consultar**: saldo, gastos, orçamentos, metas, transações.\n' +
        '- **Lançar**: despesas, receitas, categorias.\n' +
        '- **Planejar**: orçamentos, metas e aportes.\n' +
        '- **Corrigir**: alterar valores, marcar como pago ou excluir. Excluir sempre pede confirmação em vermelho.'
    });
    this.scrollDown();
  }

  loadSettings(): void {
    this.assistant.getSettings().subscribe({
      next: (s) => {
        this.settings = s;
        this.cdr.markForCheck();
      },
      error: () => (this.settings = null)
    });
    this.assistant.getVoiceSettings().subscribe({
      next: (v) => {
        this.voiceSettings = v;
        this.cdr.markForCheck();
      },
      // Back antigo ou erro: sem voz, o chat de texto segue normal
      error: () => (this.voiceSettings = null)
    });
  }

  openVoiceConsent(): void {
    this.voiceConsentOpen = true;
    this.voiceAcknowledged = false;
  }

  closeVoiceConsent(): void {
    this.voiceConsentOpen = false;
    this.voiceAcknowledged = false;
  }

  setVoiceEnabled(enabled: boolean): void {
    if (this.savingSettings || (enabled && !this.voiceAcknowledged)) {
      return;
    }
    this.savingSettings = true;
    this.assistant.updateVoiceSettings(enabled, this.voiceAcknowledged).subscribe({
      next: (v) => {
        this.voiceSettings = v;
        this.savingSettings = false;
        this.closeVoiceConsent();
        this.reply({
          from: 'assistant',
          local: true,
          text: v.enabled
            ? 'Perguntas por voz ativadas. Toque no microfone, fale e toque de novo para enviar.'
            : 'Perguntas por voz desativadas.'
        });
      },
      error: () => {
        this.savingSettings = false;
        this.reply({ from: 'assistant', local: true, text: 'Não consegui alterar a voz. Tente de novo.' });
      }
    });
  }

  /** Um toque começa a gravar; outro toque (ou 60 s) para e envia. Funciona com Enter/Espaço por ser um botão. */
  toggleRecording(): void {
    if (this.micState === 'recording') {
      this.stopRecording();
    } else if (this.micState === 'idle' && !this.loading) {
      void this.startRecording();
    }
  }

  async startRecording(): Promise<void> {
    if (!this.canUseVoice) {
      return;
    }
    try {
      this.stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    } catch {
      this.micDenied = true;
      this.reply({
        from: 'assistant',
        local: true,
        text: 'Não tenho acesso ao microfone. Libere a permissão no navegador (ícone de cadeado na barra de endereço) ou continue digitando.'
      });
      return;
    }
    const type = RECORD_TYPES.find((t) => MediaRecorder.isTypeSupported?.(t));
    this.recorder = new MediaRecorder(this.stream, type ? { mimeType: type } : undefined);
    this.chunks = [];
    this.recorder.ondataavailable = (e) => {
      if (e.data && e.data.size > 0) {
        this.chunks.push(e.data);
      }
    };
    this.recorder.onstop = () => this.onRecordingStopped();
    this.recordStart = Date.now();
    this.recordedSeconds = 0;
    this.recorder.start();
    this.micState = 'recording';
    this.voiceStatus = 'Gravando. Fale sua pergunta e toque no microfone para enviar.';
    this.tickTimer = setInterval(() => {
      this.recordedSeconds = Math.floor((Date.now() - this.recordStart) / 1000);
      this.cdr.markForCheck();
    }, 500);
    this.recordTimer = setTimeout(() => this.stopRecording(), AssistantWidgetComponent.MAX_RECORDING_MS);
    this.cdr.markForCheck();
  }

  stopRecording(): void {
    if (this.recorder && this.recorder.state !== 'inactive') {
      this.recorder.stop();
    }
    this.clearTimers();
  }

  ngOnDestroy(): void {
    if (this.recorder) {
      this.recorder.onstop = null;
    }
    this.stopRecording();
    this.releaseMic();
  }

  private onRecordingStopped(): void {
    const durationMs = Math.min(Date.now() - this.recordStart, AssistantWidgetComponent.MAX_RECORDING_MS);
    const type = this.recorder?.mimeType || this.chunks[0]?.type || 'audio/webm';
    const audio = new Blob(this.chunks, { type });
    this.chunks = [];
    this.releaseMic();
    if (audio.size === 0 || durationMs < 300) {
      this.micState = 'idle';
      this.voiceStatus = '';
      this.reply({ from: 'assistant', local: true, text: 'Não captei nada. Toque no microfone e fale um pouco mais.' });
      return;
    }
    this.sendVoice(audio, durationMs);
  }

  private sendVoice(audio: Blob, durationMs: number): void {
    const history = this.history();
    this.micState = 'processing';
    this.voiceStatus = 'Processando o áudio.';
    this.loading = true;
    this.scrollDown();
    this.assistant.askByVoice(audio, durationMs, history).subscribe({
      next: (res) => {
        this.micState = 'idle';
        this.voiceStatus = '';
        this.messages.push({ from: 'user', text: res.transcript, voice: true });
        this.reply({
          from: 'assistant',
          text: res.answer,
          ai: res.ai,
          actions: (res.actions ?? []).map((a) => ({ ...a, state: 'pending' as const })),
          audioSrc: res.audio ? `data:${res.audio.mimeType};base64,${res.audio.base64}` : undefined,
          autoplay: !!res.audio
        });
      },
      error: (err) => {
        this.micState = 'idle';
        this.voiceStatus = '';
        this.reply({
          from: 'assistant',
          local: true,
          text: err?.error?.message || err?.error?.detail || 'Não consegui processar o áudio. Tente de novo ou digite a pergunta.'
        });
      }
    });
  }

  private releaseMic(): void {
    this.stream?.getTracks().forEach((t) => t.stop());
    this.stream = null;
  }

  private clearTimers(): void {
    if (this.recordTimer) {
      clearTimeout(this.recordTimer);
      this.recordTimer = null;
    }
    if (this.tickTimer) {
      clearInterval(this.tickTimer);
      this.tickTimer = null;
    }
  }

  openConsent(): void {
    this.consentOpen = true;
    this.acknowledged = false;
  }

  closeConsent(): void {
    this.consentOpen = false;
    this.acknowledged = false;
  }

  setEnabled(enabled: boolean): void {
    if (this.savingSettings || (enabled && !this.acknowledged)) {
      return;
    }
    this.savingSettings = true;
    this.assistant.updateSettings(enabled, this.acknowledged).subscribe({
      next: (s) => {
        this.settings = s;
        this.savingSettings = false;
        this.closeConsent();
        this.reply({
          from: 'assistant',
          local: true,
          text: s.enabled
            ? 'Assistente com IA ativado para a sua família. Pode perguntar!'
            : 'Assistente com IA desativado. Continuo respondendo dúvidas básicas de uso.'
        });
      },
      error: () => {
        this.savingSettings = false;
        this.reply({ from: 'assistant', local: true, text: 'Não consegui alterar a configuração. Tente de novo.' });
      }
    });
  }

  send(question: string): void {
    const text = question.trim();
    if (!text || this.loading) {
      return;
    }
    const history = this.history();
    this.messages.push({ from: 'user', text });
    this.loading = true;
    this.scrollDown();
    this.assistant.ask(text, history).subscribe({
      next: (res) =>
        this.reply({
          from: 'assistant',
          text: res.answer,
          ai: res.ai,
          actions: (res.actions ?? []).map((a) => ({ ...a, state: 'pending' as const }))
        }),
      error: (err) =>
        this.reply({
          from: 'assistant',
          text:
            err?.status === 429
              ? 'Muitas perguntas seguidas. Aguarde um instante e tente de novo.'
              : 'Não consegui responder agora. Tente novamente em instantes.'
        })
    });
  }

  submit(field: HTMLInputElement | HTMLTextAreaElement): void {
    const value = field.value;
    field.value = '';
    this.typed = 0;
    field.style.height = '';
    this.send(value);
  }

  /** Enter envia; Shift+Enter quebra a linha. */
  onKey(event: KeyboardEvent, field: HTMLTextAreaElement): void {
    if (event.key === 'Enter' && !event.shiftKey && !event.isComposing) {
      event.preventDefault();
      this.submit(field);
    }
  }

  /** A caixa cresce com o texto, até um limite, e conta os caracteres. */
  onInput(field: HTMLTextAreaElement): void {
    this.typed = field.value.length;
    field.style.height = 'auto';
    field.style.height = Math.min(field.scrollHeight, 120) + 'px';
  }

  confirm(action: ChatAction): void {
    if (action.state !== 'pending') {
      return;
    }
    action.state = 'working';
    this.assistant.confirm(action.id).subscribe({
      next: (res) => {
        action.state = 'done';
        this.reply({ from: 'assistant', text: res.message });
        this.reloadCurrentPage();
      },
      error: (err) => {
        action.state = 'failed';
        this.reply({
          from: 'assistant',
          text: err?.error?.message || 'Não consegui executar essa ação. Ela pode ter expirado; peça de novo.'
        });
      }
    });
  }

  cancel(action: ChatAction): void {
    if (action.state !== 'pending') {
      return;
    }
    action.state = 'canceled';
    this.assistant.cancel(action.id).subscribe({ error: () => undefined });
  }

  /** Quebra o texto em parágrafos e itens de lista, com **negrito**. Nunca gera HTML. */
  format(text: string): Block[] {
    return text
      .split('\n')
      .map((line) => line.trimEnd())
      .filter((line) => line.length > 0)
      .map((line) => {
        const item = /^\s*[-•*]\s+(.*)$/.exec(line);
        return { type: item ? ('li' as const) : ('p' as const), parts: this.bold(item ? item[1] : line) };
      });
  }

  private bold(text: string): Part[] {
    return text
      .split(/\*\*(.+?)\*\*/g)
      .map((chunk, i) => ({ text: chunk, bold: i % 2 === 1 }))
      .filter((p) => p.text.length > 0);
  }

  private welcome(): ChatMessage {
    return {
      from: 'assistant',
      local: true,
      text:
        'Oi! Eu **consulto os dados da sua família**, tiro dúvidas e **faço coisas por você**: lançar despesas, criar metas e orçamentos, corrigir ou excluir lançamentos.\n' +
        'Antes de alterar qualquer coisa, eu peço a sua confirmação.'
    };
  }

  /** Só texto de usuário e de assistente vai como histórico; mensagens locais e cartões de ação ficam de fora. */
  private history(): Turn[] {
    return this.messages.filter((m) => !m.local).map((m) => ({ role: m.from, text: m.text }));
  }

  /** Depois de uma ação executada, recarrega a tela atual para ela mostrar o dado novo. */
  private reloadCurrentPage(): void {
    const reuse = this.router.routeReuseStrategy.shouldReuseRoute;
    const previous = this.router.onSameUrlNavigation;
    // Mantém a raiz e as rotas-pai (o shell, onde mora o chat) e recria só a página da folha, que busca os dados de novo.
    this.router.routeReuseStrategy.shouldReuseRoute = (future, curr) => {
      const cfg = future.routeConfig;
      return future.routeConfig === curr.routeConfig && (cfg === null || !!cfg.children?.length || !!cfg.loadChildren);
    };
    this.router.onSameUrlNavigation = 'reload';
    this.router.navigateByUrl(this.router.url).finally(() => {
      this.router.routeReuseStrategy.shouldReuseRoute = reuse;
      this.router.onSameUrlNavigation = previous;
    });
  }

  private reply(message: ChatMessage): void {
    this.messages.push(message);
    this.loading = false;
    this.cdr.markForCheck();
    this.scrollDown();
  }

  /** Rola até o fim depois que o Angular desenhar a mensagem; a segunda passada cobre o cartão de ação e as listas. */
  private scrollDown(): void {
    const down = () => {
      const el = this.list?.nativeElement;
      if (el) {
        el.scrollTop = el.scrollHeight;
      }
    };
    setTimeout(down);
    setTimeout(down, 80);
  }

  private markSeen(): void {
    this.seen = true;
    try {
      localStorage.setItem('controlei-assistant-seen', '1');
    } catch {
      // armazenamento bloqueado: o ícone só volta a chamar atenção na próxima visita
    }
  }

  private static readSeen(): boolean {
    try {
      return localStorage.getItem('controlei-assistant-seen') === '1';
    } catch {
      return false;
    }
  }
}
