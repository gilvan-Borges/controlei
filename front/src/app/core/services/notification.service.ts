import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Notification,
  CreateNotificationRequest,
  UnreadNotificationsCount
} from '../models/notification.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly baseUrl = `${environment.apiUrl}/notifications`;

  constructor(private http: HttpClient) {}

  listNotifications(unreadOnly = false): Observable<Notification[]> {
    const params = new HttpParams().set('unreadOnly', unreadOnly.toString());
    return this.http.get<Notification[]>(this.baseUrl, { params });
  }

  getUnreadCount(): Observable<UnreadNotificationsCount> {
    return this.http.get<UnreadNotificationsCount>(`${this.baseUrl}/unread-count`);
  }

  createNotification(request: CreateNotificationRequest): Observable<Notification> {
    return this.http.post<Notification>(this.baseUrl, request);
  }

  markAsRead(id: string): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${id}/read`, {});
  }

  markAllAsRead(): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/mark-all-read`, {});
  }
}
