import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface PreparedAction {
  id: string;
  summary: string;
  /** Exclusão: a tela destaca o cartão e o botão diz "Excluir". */
  destructive?: boolean;
}

export interface AssistantSettings {
  enabled: boolean;
  /** Só o responsável da família pode ligar ou desligar. */
  canManage: boolean;
  /** Há provedor de IA configurado no servidor. */
  aiAvailable: boolean;
}

export interface AssistantAnswer {
  answer: string;
  /** true quando a resposta veio do modelo; false quando veio da base local de ajuda. */
  ai: boolean;
  /** Ações que o assistente preparou e que só valem depois que a pessoa confirmar. */
  actions: PreparedAction[];
}

/** Voz do assistente: interruptor próprio da família (o áudio sai do servidor para um provedor externo). */
export interface VoiceSettings {
  enabled: boolean;
  /** Só o responsável liga ou desliga, com aceite. */
  canManage: boolean;
  /** O servidor tem provedor de voz configurado. */
  voiceAvailable: boolean;
  /** A voz só funciona com o assistente também ligado. */
  assistantEnabled: boolean;
}

export interface VoiceAnswer extends AssistantAnswer {
  /** O que o servidor entendeu da fala: vira a mensagem do usuário no chat. */
  transcript: string;
  /** Resposta falada (MP3). null quando a síntese falhou: a resposta continua em texto. */
  audio: { mimeType: string; base64: string } | null;
}

export interface Turn {
  role: 'user' | 'assistant';
  text: string;
}

@Injectable({
  providedIn: 'root'
})
export class AssistantService {
  private readonly baseUrl = `${environment.apiUrl}/assistant`;

  constructor(private http: HttpClient) {}

  ask(question: string, history: Turn[]): Observable<AssistantAnswer> {
    return this.http.post<AssistantAnswer>(`${this.baseUrl}/ask`, { question, history });
  }

  getSettings(): Observable<AssistantSettings> {
    return this.http.get<AssistantSettings>(`${this.baseUrl}/settings`);
  }

  updateSettings(enabled: boolean, acknowledged: boolean): Observable<AssistantSettings> {
    return this.http.put<AssistantSettings>(`${this.baseUrl}/settings`, { enabled, acknowledged });
  }

  getVoiceSettings(): Observable<VoiceSettings> {
    return this.http.get<VoiceSettings>(`${this.baseUrl}/voice/settings`);
  }

  updateVoiceSettings(enabled: boolean, acknowledged: boolean): Observable<VoiceSettings> {
    return this.http.put<VoiceSettings>(`${this.baseUrl}/voice/settings`, { enabled, acknowledged });
  }

  /** Pergunta falada: o servidor transcreve e responde pelo mesmo assistente do texto. */
  askByVoice(audio: Blob, durationMs: number, history: Turn[]): Observable<VoiceAnswer> {
    const form = new FormData();
    form.append('audio', audio, 'pergunta.' + extensionOf(audio.type));
    form.append('history', JSON.stringify(history));
    form.append('durationMs', String(Math.round(durationMs)));
    return this.http.post<VoiceAnswer>(`${this.baseUrl}/voice`, form);
  }

  confirm(actionId: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/actions/${actionId}/confirm`, {});
  }

  cancel(actionId: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/actions/${actionId}/cancel`, {});
  }
}

/** Extensão do nome do arquivo pelo tipo gravado (o servidor confere o conteúdo de qualquer forma). */
function extensionOf(type: string): string {
  if (type.includes('ogg')) return 'ogg';
  if (type.includes('mp4') || type.includes('m4a')) return 'm4a';
  if (type.includes('mpeg')) return 'mp3';
  if (type.includes('wav')) return 'wav';
  return 'webm';
}
