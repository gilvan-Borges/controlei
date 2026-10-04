import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AssistantAnswer {
  answer: string;
  /** true quando a resposta veio do modelo; false quando veio da base local de ajuda. */
  ai: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AssistantService {
  private readonly baseUrl = `${environment.apiUrl}/assistant`;

  constructor(private http: HttpClient) {}

  ask(question: string): Observable<AssistantAnswer> {
    return this.http.post<AssistantAnswer>(`${this.baseUrl}/ask`, { question });
  }
}
