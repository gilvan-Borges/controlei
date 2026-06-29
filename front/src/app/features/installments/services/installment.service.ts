import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Installment, PayInstallmentResponse, InstallmentStatus } from '../../../core/models/installment.model';

@Injectable({
  providedIn: 'root'
})
export class InstallmentService {
  private baseUrl = `${environment.apiUrl}/installments`;

  constructor(private http: HttpClient) {}

  listInstallments(filters?: {
    userId?: string;
    debtId?: string;
    status?: InstallmentStatus;
    startDate?: string;
    endDate?: string;
  }): Observable<Installment[]> {
    let params = new HttpParams();
    if (filters?.userId) params = params.set('userId', filters.userId);
    if (filters?.debtId) params = params.set('debtId', filters.debtId);
    if (filters?.status) params = params.set('status', filters.status);
    if (filters?.startDate) params = params.set('startDate', filters.startDate);
    if (filters?.endDate) params = params.set('endDate', filters.endDate);
    return this.http.get<Installment[]>(this.baseUrl, { params });
  }

  payInstallment(id: string): Observable<PayInstallmentResponse> {
    return this.http.put<PayInstallmentResponse>(`${this.baseUrl}/${id}/pay`, {});
  }

  cancelInstallment(id: string): Observable<PayInstallmentResponse> {
    return this.http.put<PayInstallmentResponse>(`${this.baseUrl}/${id}/cancel`, {});
  }
}
