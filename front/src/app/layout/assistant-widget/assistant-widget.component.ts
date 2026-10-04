import { ChangeDetectorRef, Component, ElementRef, HostListener, ViewChild } from '@angular/core';
import { AssistantService } from '../../core/services/assistant.service';

export interface ChatMessage {
  from: 'user' | 'assistant';
  text: string;
  ai?: boolean;
}

/**
 * Assistente flutuante de ajuda. Responde duvidas de uso do app; nao recebe nem mostra dados financeiros.
 * Acessivel: botao com rotulo, painel como dialogo, Esc fecha e devolve o foco, respostas anunciadas por aria-live.
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
    'Como lançar uma despesa?',
    'Como funciona o comprovante com IA?',
    'Como dividir uma conta?',
    'Como instalar no celular?'
  ];

  open = false;
  loading = false;
  messages: ChatMessage[] = [
    { from: 'assistant', text: 'Oi! Sou o assistente do Controlei. Tire suas dúvidas sobre como usar o app.' }
  ];

  @ViewChild('input') input?: ElementRef<HTMLInputElement>;
  @ViewChild('list') list?: ElementRef<HTMLElement>;
  @ViewChild('fab') fab?: ElementRef<HTMLButtonElement>;

  constructor(private assistant: AssistantService, private cdr: ChangeDetectorRef) {}

  toggle(): void {
    this.open = !this.open;
    if (this.open) {
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

  send(question: string): void {
    const text = question.trim();
    if (!text || this.loading) {
      return;
    }
    this.messages.push({ from: 'user', text });
    this.loading = true;
    this.scrollDown();
    this.assistant.ask(text).subscribe({
      next: (res) => this.reply(res.answer, res.ai),
      error: (err) =>
        this.reply(
          err?.status === 429
            ? 'Muitas perguntas seguidas. Aguarde um instante e tente de novo.'
            : 'Não consegui responder agora. Tente novamente em instantes.'
        )
    });
  }

  submit(field: HTMLInputElement): void {
    const value = field.value;
    field.value = '';
    this.send(value);
  }

  private reply(text: string, ai = false): void {
    this.messages.push({ from: 'assistant', text, ai });
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
