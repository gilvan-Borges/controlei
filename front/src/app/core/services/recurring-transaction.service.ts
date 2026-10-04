import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  RecurringTransaction,
  CreateRecurringTransactionRequest,
  UpdateRecurringTransactionRequest
} from '../models/recurring-transaction.model';

@Injectable({
  providedIn: 'root'
})
export class RecurringTransactionService {
  private readonly baseUrl = `${environment.apiUrl}/recurring-transactions`;

  constructor(private http: HttpClient) {}

  list(): Observable<RecurringTransaction[]> {
    return this.http.get<RecurringTransaction[]>(this.baseUrl);
  }

  getById(id: string): Observable<RecurringTransaction> {
    return this.http.get<RecurringTransaction>(`${this.baseUrl}/${id}`);
  }

  create(request: CreateRecurringTransactionRequest): Observable<RecurringTransaction> {
    return this.http.post<RecurringTransaction>(this.baseUrl, request);
  }

  update(id: string, request: UpdateRecurringTransactionRequest): Observable<RecurringTransaction> {
    return this.http.put<RecurringTransaction>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  toggleActive(id: string): Observable<RecurringTransaction> {
    return this.http.patch<RecurringTransaction>(`${this.baseUrl}/${id}/toggle`, {});
  }
}
