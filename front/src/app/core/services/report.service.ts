import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TaxDeclarationReport } from '../models/report.model';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private readonly baseUrl = `${environment.apiUrl}/reports`;

  constructor(private http: HttpClient) {}

  downloadMonthlyStatementCsv(year: number, month: number): Observable<Blob> {
    const params = new HttpParams().set('year', year.toString()).set('month', month.toString());
    return this.http.get(`${this.baseUrl}/monthly-statement`, {
      params,
      responseType: 'blob'
    });
  }

  getTaxDeclaration(year: number): Observable<TaxDeclarationReport> {
    const params = new HttpParams().set('year', year.toString());
    return this.http.get<TaxDeclarationReport>(`${this.baseUrl}/tax-declaration`, { params });
  }
}
