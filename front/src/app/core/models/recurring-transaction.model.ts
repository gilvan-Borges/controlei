export type RecurrenceFrequency = 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY';
export type TransactionType = 'INCOME' | 'EXPENSE';

export interface RecurringTransaction {
  id: string;
  familyId: string;
  userId: string;
  userName?: string;
  accountId: string;
  accountName?: string;
  categoryId?: string;
  categoryName?: string;
  type: TransactionType;
  description: string;
  amount: number;
  frequency: RecurrenceFrequency;
  dayOfMonth?: number;
  startDate: string;
  endDate?: string;
  nextExecutionDate: string;
  autoPay: boolean;
  active: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface CreateRecurringTransactionRequest {
  userId?: string;
  accountId: string;
  categoryId?: string;
  type: TransactionType;
  description: string;
  amount: number;
  frequency: RecurrenceFrequency;
  dayOfMonth?: number;
  startDate: string;
  endDate?: string;
  autoPay?: boolean;
}

export interface UpdateRecurringTransactionRequest {
  accountId: string;
  categoryId?: string;
  type: TransactionType;
  description: string;
  amount: number;
  frequency: RecurrenceFrequency;
  dayOfMonth?: number;
  startDate: string;
  endDate?: string;
  autoPay?: boolean;
  active?: boolean;
}
