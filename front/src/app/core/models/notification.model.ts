export type NotificationType =
  | 'BILL_DUE'
  | 'BUDGET_WARNING'
  | 'INVOICE_CLOSED'
  | 'GOAL_REACHED'
  | 'SYSTEM';

export interface Notification {
  id: string;
  familyId: string;
  userId: string;
  title: string;
  message: string;
  type: NotificationType;
  linkUrl?: string;
  read: boolean;
  readAt?: string;
  createdAt: string;
}

export interface CreateNotificationRequest {
  userId?: string;
  title: string;
  message: string;
  type: NotificationType;
  linkUrl?: string;
}

export interface UnreadNotificationsCount {
  unreadCount: number;
}
