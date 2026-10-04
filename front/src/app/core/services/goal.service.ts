import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  FinancialGoal,
  CreateGoalRequest,
  UpdateGoalRequest,
  CreateGoalContributionRequest,
  GoalContribution,
  WithdrawGoalRequest
} from '../models/goal.model';

@Injectable({
  providedIn: 'root'
})
export class GoalService {
  private readonly baseUrl = `${environment.apiUrl}/goals`;

  constructor(private http: HttpClient) {}

  listGoals(): Observable<FinancialGoal[]> {
    return this.http.get<FinancialGoal[]>(this.baseUrl);
  }

  getGoal(id: string): Observable<FinancialGoal> {
    return this.http.get<FinancialGoal>(`${this.baseUrl}/${id}`);
  }

  createGoal(request: CreateGoalRequest): Observable<FinancialGoal> {
    return this.http.post<FinancialGoal>(this.baseUrl, request);
  }

  updateGoal(id: string, request: UpdateGoalRequest): Observable<FinancialGoal> {
    return this.http.put<FinancialGoal>(`${this.baseUrl}/${id}`, request);
  }

  deleteGoal(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  addContribution(goalId: string, request: CreateGoalContributionRequest): Observable<GoalContribution> {
    return this.http.post<GoalContribution>(`${this.baseUrl}/${goalId}/contributions`, request);
  }

  listContributions(goalId: string): Observable<GoalContribution[]> {
    return this.http.get<GoalContribution[]>(`${this.baseUrl}/${goalId}/contributions`);
  }

  withdraw(goalId: string, request: WithdrawGoalRequest): Observable<FinancialGoal> {
    return this.http.post<FinancialGoal>(`${this.baseUrl}/${goalId}/withdraw`, request);
  }
}
