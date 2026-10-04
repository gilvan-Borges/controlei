import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { IndividualDashboardResponse, FamilyDashboardResponse } from '../../../core/models/dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private baseUrl = `${environment.apiUrl}/dashboard`;

  constructor(private http: HttpClient) {}

  getIndividual(year?: number, month?: number): Observable<IndividualDashboardResponse> {
    let params = new HttpParams();
    if (year && month) {
      const startDate = `${year}-${String(month).padStart(2, '0')}-01`;
      const lastDay = new Date(year, month, 0).getDate();
      const endDate = `${year}-${String(month).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`;
      params = params.set('startDate', startDate).set('endDate', endDate);
    }
    return this.http.get<IndividualDashboardResponse>(`${this.baseUrl}/individual`, { params });
  }

  getFamily(year?: number, month?: number): Observable<FamilyDashboardResponse> {
    let params = new HttpParams();
    if (year && month) {
      const startDate = `${year}-${String(month).padStart(2, '0')}-01`;
      const lastDay = new Date(year, month, 0).getDate();
      const endDate = `${year}-${String(month).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`;
      params = params.set('startDate', startDate).set('endDate', endDate);
    }
    return this.http.get<FamilyDashboardResponse>(`${this.baseUrl}/family`, { params });
  }
}
