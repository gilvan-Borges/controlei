import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Investment, CreateInvestmentRequest, UpdateInvestmentRequest, InvestmentType } from '../../../core/models/investment.model';

@Injectable({
  providedIn: 'root'
})
export class InvestmentService {
  private baseUrl = `${environment.apiUrl}/investments`;

  constructor(private http: HttpClient) {}

  listInvestments(filters?: {
    userId?: string;
    type?: InvestmentType;
    categoryId?: string;
    startDate?: string;
    endDate?: string;
  }): Observable<Investment[]> {
    let params = new HttpParams();
    if (filters?.userId) params = params.set('userId', filters.userId);
    if (filters?.type) params = params.set('type', filters.type);
    if (filters?.categoryId) params = params.set('categoryId', filters.categoryId);
    if (filters?.startDate) params = params.set('startDate', filters.startDate);
    if (filters?.endDate) params = params.set('endDate', filters.endDate);
    return this.http.get<Investment[]>(this.baseUrl, { params });
  }

  getInvestment(id: string): Observable<Investment> {
    return this.http.get<Investment>(`${this.baseUrl}/${id}`);
  }

  createInvestment(request: CreateInvestmentRequest): Observable<Investment> {
    return this.http.post<Investment>(this.baseUrl, request);
  }

  updateInvestment(id: string, request: UpdateInvestmentRequest): Observable<Investment> {
    return this.http.put<Investment>(`${this.baseUrl}/${id}`, request);
  }

  deleteInvestment(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
