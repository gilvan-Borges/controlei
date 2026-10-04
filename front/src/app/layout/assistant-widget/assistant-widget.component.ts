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
  /** Mensagem local (não vai ao servidor nem entra no histórico enviado ao modelo). */
  local?: boolean;
  /** Mensagem da lista "o que sei fazer": mostra os exemplos tocáveis logo abaixo. */
  caps?: boolean;
}

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
export class AssistantWidgetComponent {
  readonly maxLength = 500;

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
