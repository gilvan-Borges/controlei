import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Budget,
  BudgetSummary,
  CreateBudgetRequest,
  UpdateBudgetRequest
} from '../models/budget.model';

@Injectable({
  providedIn: 'root'
})
export class BudgetService {
  private readonly baseUrl = `${environment.apiUrl}/budgets`;

  constructor(private http: HttpClient) {}

  list(year: number, month: number): Observable<Budget[]> {
    const params = new HttpParams().set('year', year).set('month', month);
    return this.http.get<Budget[]>(this.baseUrl, { params });
  }

  getSummary(year: number, month: number): Observable<BudgetSummary> {
    const params = new HttpParams().set('year', year).set('month', month);
    return this.http.get<BudgetSummary>(`${this.baseUrl}/summary`, { params });
  }

  getById(id: string): Observable<Budget> {
    return this.http.get<Budget>(`${this.baseUrl}/${id}`);
  }

  create(request: CreateBudgetRequest): Observable<Budget> {
    return this.http.post<Budget>(this.baseUrl, request);
  }

  update(id: string, request: UpdateBudgetRequest): Observable<Budget> {
    return this.http.put<Budget>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
