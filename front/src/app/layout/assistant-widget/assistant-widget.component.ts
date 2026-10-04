import { ChangeDetectorRef, Component, ElementRef, HostListener, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { AssistantService, AssistantSettings, PreparedAction, Turn } from '../../core/services/assistant.service';

export interface ChatAction extends PreparedAction {
  state: 'pending' | 'working' | 'done' | 'canceled' | 'failed';
}

export interface ChatMessage {
  from: 'user' | 'assistant';
  text: string;
  ai?: boolean;
  actions?: ChatAction[];
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
export class AssistantWidgetComponent {
  readonly maxLength = 500;
  readonly suggestions = [
    'Quanto gastei este mês?',
    'Lance uma despesa de 50 reais no mercado',
    'Como estão meus orçamentos?',
    'Como dividir uma conta?'
  ];

  open = false;
  loading = false;
  /** Já abriu o assistente alguma vez: depois disso o ícone para de chamar atenção. */
  seen = AssistantWidgetComponent.readSeen();
  settings: AssistantSettings | null = null;
  /** Painel de ciência aberto: o responsável precisa aceitar antes de ligar a IA. */
  consentOpen = false;
  acknowledged = false;
  savingSettings = false;
  messages: ChatMessage[] = [
    {
      from: 'assistant',
      text: 'Oi! Posso responder sobre seus dados, tirar dúvidas e fazer coisas por você, como lançar despesas ou criar metas. Antes de alterar qualquer coisa, peço sua confirmação.'
    }
  ];

  @ViewChild('input') input?: ElementRef<HTMLInputElement>;
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

  loadSettings(): void {
    this.assistant.getSettings().subscribe({
      next: (s) => {
        this.settings = s;
        this.cdr.markForCheck();
      },
      error: () => (this.settings = null)
    });
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
          text: s.enabled
            ? 'Assistente com IA ativado para a sua família. Pode perguntar!'
            : 'Assistente com IA desativado. Continuo respondendo dúvidas básicas de uso.'
        });
      },
      error: () => {
        this.savingSettings = false;
        this.reply({ from: 'assistant', text: 'Não consegui alterar a configuração. Tente de novo.' });
      }
    });
  }

  @HostListener('document:keydown.escape')
  close(): void {
    if (this.open) {
      this.open = false;
      this.fab?.nativeElement.focus();
    }
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

  submit(field: HTMLInputElement): void {
    const value = field.value;
    field.value = '';
    this.send(value);
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

  /** Só texto de usuário e de assistente vai como histórico; os cartões de ação ficam de fora. */
  private history(): Turn[] {
    return this.messages.slice(1).map((m) => ({ role: m.from, text: m.text }));
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

  private scrollDown(): void {
    setTimeout(() => {
      const el = this.list?.nativeElement;
      if (el) {
        el.scrollTop = el.scrollHeight;
      }
    });
  }
}
