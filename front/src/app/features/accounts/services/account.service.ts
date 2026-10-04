import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Account, CreateAccountRequest, UpdateAccountRequest, AccountType } from '../../../core/models/account.model';

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  private baseUrl = `${environment.apiUrl}/accounts`;

  constructor(private http: HttpClient) {}

  listAccounts(filters?: { active?: boolean; type?: AccountType; shared?: boolean; userId?: string }): Observable<Account[]> {
    let params = new HttpParams();
    if (filters?.active !== undefined && filters.active !== null) {
      params = params.set('active', String(filters.active));
    }
    if (filters?.type) {
      params = params.set('type', filters.type);
    }
    if (filters?.shared !== undefined && filters.shared !== null) {
      params = params.set('shared', String(filters.shared));
    }
    if (filters?.userId) {
      params = params.set('userId', filters.userId);
    }
    return this.http.get<Account[]>(this.baseUrl, { params });
  }

  getAccount(id: string): Observable<Account> {
    return this.http.get<Account>(`${this.baseUrl}/${id}`);
  }

  createAccount(request: CreateAccountRequest): Observable<Account> {
    return this.http.post<Account>(this.baseUrl, request);
  }

  updateAccount(id: string, request: UpdateAccountRequest): Observable<Account> {
    return this.http.put<Account>(`${this.baseUrl}/${id}`, request);
  }

  deleteAccount(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
