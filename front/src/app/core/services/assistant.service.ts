import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface PreparedAction {
  id: string;
  summary: string;
}

export interface AssistantAnswer {
  answer: string;
  /** true quando a resposta veio do modelo; false quando veio da base local de ajuda. */
  ai: boolean;
  /** Ações que o assistente preparou e que só valem depois que a pessoa confirmar. */
  actions: PreparedAction[];
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

  confirm(actionId: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.baseUrl}/actions/${actionId}/confirm`, {});
  }

  cancel(actionId: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/actions/${actionId}/cancel`, {});
  }
}
