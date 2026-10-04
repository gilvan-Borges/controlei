import { Component, OnInit } from '@angular/core';
import { NotificationService } from '../../../core/services/notification.service';
import { Notification } from '../../../core/models/notification.model';

@Component({
  selector: 'app-notifications-page',
  standalone: false,
  templateUrl: './notifications-page.component.html',
  styleUrl: './notifications-page.component.scss'
})
export class NotificationsPageComponent implements OnInit {
  notifications: Notification[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';
  unreadOnly = false;

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    this.loadNotifications();
  }

  loadNotifications(): void {
    this.loading = true;
    this.errorMessage = '';
    this.notificationService.listNotifications(this.unreadOnly).subscribe({
      next: (data) => {
        this.notifications = data;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = 'Erro ao carregar notificações: ' + (err.error?.message || err.message);
        this.loading = false;
      }
    });
  }

  toggleFilter(unread: boolean): void {
    this.unreadOnly = unread;
    this.loadNotifications();
  }

  markRead(notif: Notification): void {
    if (notif.read) return;
    this.notificationService.markAsRead(notif.id).subscribe({
      next: () => {
        notif.read = true;
      }
    });
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.successMessage = 'Todas as notificações foram marcadas como lidas!';
        this.loadNotifications();
        setTimeout(() => (this.successMessage = ''), 4000);
      }
    });
  }

  getIcon(type: string): string {
    switch (type) {
      case 'BILL_DUE': return 'bi-calendar-event text-warning';
      case 'BUDGET_WARNING': return 'bi-exclamation-octagon text-danger';
      case 'INVESTMENT_DIVIDEND': return 'bi-graph-up text-success';
      case 'GOAL_ACHIEVED': return 'bi-trophy text-warning';
      case 'EXPENSE_SPLIT': return 'bi-people text-info';
      default: return 'bi-bell text-primary';
    }
  }
}
