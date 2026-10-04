import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Transaction, CreateTransactionRequest, UpdateTransactionRequest, PageResult, TransactionType, TransactionStatus } from '../../../core/models/transaction.model';

@Injectable({
  providedIn: 'root'
})
export class TransactionService {
  private baseUrl = `${environment.apiUrl}/transactions`;

  constructor(private http: HttpClient) {}

  listTransactions(filters?: {
    startDate?: string;
    endDate?: string;
    userId?: string;
    accountId?: string;
    categoryId?: string;
    type?: TransactionType;
    status?: TransactionStatus;
    page?: number;
    size?: number;
  }): Observable<PageResult<Transaction>> {
    let params = new HttpParams();
    if (filters?.startDate) params = params.set('startDate', filters.startDate);
    if (filters?.endDate) params = params.set('endDate', filters.endDate);
    if (filters?.userId) params = params.set('userId', filters.userId);
    if (filters?.accountId) params = params.set('accountId', filters.accountId);
    if (filters?.categoryId) params = params.set('categoryId', filters.categoryId);
    if (filters?.type) params = params.set('type', filters.type);
    if (filters?.status) params = params.set('status', filters.status);
    if (filters?.page !== undefined) params = params.set('page', String(filters.page));
    if (filters?.size !== undefined) params = params.set('size', String(filters.size));
    return this.http.get<PageResult<Transaction>>(this.baseUrl, { params });
  }

  getTransaction(id: string): Observable<Transaction> {
    return this.http.get<Transaction>(`${this.baseUrl}/${id}`);
  }

  createTransaction(request: CreateTransactionRequest): Observable<Transaction> {
    return this.http.post<Transaction>(this.baseUrl, request);
  }

  updateTransaction(id: string, request: UpdateTransactionRequest): Observable<Transaction> {
    return this.http.put<Transaction>(`${this.baseUrl}/${id}`, request);
  }

  deleteTransaction(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  payTransaction(id: string): Observable<Transaction> {
    return this.http.put<Transaction>(`${this.baseUrl}/${id}/pay`, {});
  }

  cancelTransaction(id: string): Observable<Transaction> {
    return this.http.put<Transaction>(`${this.baseUrl}/${id}/cancel`, {});
  }
}
