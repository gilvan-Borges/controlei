import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ExpenseSplit,
  CreateSplitRequest,
  FamilyBalance,
  SettleDebtRequest,
  SplitSettlement
} from '../models/split.model';

@Injectable({
  providedIn: 'root'
})
export class SplitService {
  private readonly baseUrl = `${environment.apiUrl}/splits`;

  constructor(private http: HttpClient) {}

  listSplits(): Observable<ExpenseSplit[]> {
    return this.http.get<ExpenseSplit[]>(this.baseUrl);
  }

  getSplit(id: string): Observable<ExpenseSplit> {
    return this.http.get<ExpenseSplit>(`${this.baseUrl}/${id}`);
  }

  createSplit(request: CreateSplitRequest): Observable<ExpenseSplit> {
    return this.http.post<ExpenseSplit>(this.baseUrl, request);
  }

  getBalances(): Observable<FamilyBalance> {
    return this.http.get<FamilyBalance>(`${this.baseUrl}/balances`);
  }

  settleDebt(request: SettleDebtRequest): Observable<SplitSettlement> {
    return this.http.post<SplitSettlement>(`${this.baseUrl}/settle`, request);
  }
}
