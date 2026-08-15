import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  BankConnection,
  ConnectBankRequest,
  SyncTransactionsResponse
} from '../models/open-finance.model';

@Injectable({
  providedIn: 'root'
})
export class OpenFinanceService {
  private readonly baseUrl = `${environment.apiUrl}/bank-connections`;

  constructor(private http: HttpClient) {}

  listConnections(): Observable<BankConnection[]> {
    return this.http.get<BankConnection[]>(this.baseUrl);
  }

  connectBank(request: ConnectBankRequest): Observable<BankConnection> {
    return this.http.post<BankConnection>(this.baseUrl, request);
  }

  disconnect(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  syncConnection(id: string): Observable<SyncTransactionsResponse> {
    return this.http.post<SyncTransactionsResponse>(`${this.baseUrl}/${id}/sync`, {});
  }
}
