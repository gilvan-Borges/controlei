import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CreditCard,
  CreateCreditCardRequest,
  UpdateCreditCardRequest,
  CreateCardExpenseRequest,
  Invoice,
  CreditCardTransaction,
  PayInvoiceRequest
} from '../models/credit-card.model';

@Injectable({
  providedIn: 'root'
})
export class CreditCardService {
  private readonly baseUrl = `${environment.apiUrl}/credit-cards`;

  constructor(private http: HttpClient) {}

  listCards(): Observable<CreditCard[]> {
    return this.http.get<CreditCard[]>(this.baseUrl);
  }

  getCard(id: string): Observable<CreditCard> {
    return this.http.get<CreditCard>(`${this.baseUrl}/${id}`);
  }

  createCard(request: CreateCreditCardRequest): Observable<CreditCard> {
    return this.http.post<CreditCard>(this.baseUrl, request);
  }

  updateCard(id: string, request: UpdateCreditCardRequest): Observable<CreditCard> {
    return this.http.put<CreditCard>(`${this.baseUrl}/${id}`, request);
  }

  deleteCard(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  addExpense(cardId: string, request: CreateCardExpenseRequest): Observable<CreditCardTransaction[]> {
    return this.http.post<CreditCardTransaction[]>(`${this.baseUrl}/${cardId}/expenses`, request);
  }

  listInvoices(cardId: string): Observable<Invoice[]> {
    return this.http.get<Invoice[]>(`${this.baseUrl}/${cardId}/invoices`);
  }

  getInvoice(invoiceId: string): Observable<Invoice> {
    return this.http.get<Invoice>(`${this.baseUrl}/invoices/${invoiceId}`);
  }

  payInvoice(invoiceId: string, request: PayInvoiceRequest): Observable<Invoice> {
    return this.http.post<Invoice>(`${this.baseUrl}/invoices/${invoiceId}/pay`, request);
  }
}
