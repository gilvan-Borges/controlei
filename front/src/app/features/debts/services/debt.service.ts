import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Debt, CreateDebtRequest, UpdateDebtRequest, DebtStatus } from '../../../core/models/debt.model';
import { Installment } from '../../../core/models/installment.model';

@Injectable({
  providedIn: 'root'
})
export class DebtService {
  private baseUrl = `${environment.apiUrl}/debts`;

  constructor(private http: HttpClient) {}

  listDebts(filters?: { userId?: string; status?: DebtStatus }): Observable<Debt[]> {
    let params = new HttpParams();
    if (filters?.userId) params = params.set('userId', filters.userId);
    if (filters?.status) params = params.set('status', filters.status);
    return this.http.get<Debt[]>(this.baseUrl, { params });
  }

  getDebt(id: string): Observable<Debt> {
    return this.http.get<Debt>(`${this.baseUrl}/${id}`);
  }

  createDebt(request: CreateDebtRequest): Observable<Debt> {
    return this.http.post<Debt>(this.baseUrl, request);
  }

  updateDebt(id: string, request: UpdateDebtRequest): Observable<Debt> {
    return this.http.put<Debt>(`${this.baseUrl}/${id}`, request);
  }

  deleteDebt(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  listInstallmentsByDebt(debtId: string): Observable<Installment[]> {
    return this.http.get<Installment[]>(`${this.baseUrl}/${debtId}/installments`);
  }
}
