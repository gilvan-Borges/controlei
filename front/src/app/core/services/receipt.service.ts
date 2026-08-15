import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ReceiptScan, SimulateScanRequest } from '../models/receipt.model';

@Injectable({
  providedIn: 'root'
})
export class ReceiptService {
  private readonly baseUrl = `${environment.apiUrl}/receipts`;

  constructor(private http: HttpClient) {}

  scanReceipt(file: File): Observable<ReceiptScan> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ReceiptScan>(`${this.baseUrl}/scan`, formData);
  }

  simulateScan(request: SimulateScanRequest): Observable<ReceiptScan> {
    return this.http.post<ReceiptScan>(`${this.baseUrl}/simulate-scan`, request);
  }

  getScan(id: string): Observable<ReceiptScan> {
    return this.http.get<ReceiptScan>(`${this.baseUrl}/${id}`);
  }

  listScans(): Observable<ReceiptScan[]> {
    return this.http.get<ReceiptScan[]>(this.baseUrl);
  }
}
