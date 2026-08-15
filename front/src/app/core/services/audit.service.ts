import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuditLog } from '../models/audit.model';

@Injectable({
  providedIn: 'root'
})
export class AuditService {
  private readonly baseUrl = `${environment.apiUrl}/audit-logs`;

  constructor(private http: HttpClient) {}

  listAuditLogs(): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(this.baseUrl);
  }
}
