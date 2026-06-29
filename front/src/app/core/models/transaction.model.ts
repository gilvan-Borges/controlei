export type TransactionType = 'INCOME' | 'EXPENSE' | 'TRANSFER';
export type TransactionStatus = 'PENDING' | 'PAID' | 'OVERDUE' | 'CANCELED';

export interface Transaction {
  id: string;
  familyId: string;
  userId: string;
  accountId: string;
  categoryId: string | null;
  type: TransactionType;
  description: string;
  amount: number;
  transactionDate: string;
  dueDate: string | null;
  paidAt: string | null;
  status: TransactionStatus;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTransactionRequest {
  userId: string;
  accountId: string;
  categoryId?: string | null;
  type: TransactionType;
  description: string;
  amount: number;
  transactionDate: string;
  dueDate?: string | null;
  notes?: string;
}

export interface UpdateTransactionRequest {
  userId: string;
  accountId: string;
  categoryId?: string | null;
  type: TransactionType;
  description: string;
  amount: number;
  transactionDate: string;
  dueDate?: string | null;
  notes?: string;
}

export interface PageResult<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
