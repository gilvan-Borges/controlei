export type DebtStatus = 'PENDING' | 'PAID' | 'CANCELED';

export interface Debt {
  id: string;
  familyId: string;
  userId: string;
  categoryId: string | null;
  description: string;
  purchaseDate: string;
  totalAmount: number;
  installmentCount: number;
  installmentAmount: number;
  status: DebtStatus;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateDebtRequest {
  userId: string;
  categoryId?: string | null;
  description: string;
  purchaseDate: string;
  totalAmount: number;
  installmentCount: number;
  firstDueDate: string;
  notes?: string;
}

export interface UpdateDebtRequest {
  categoryId?: string | null;
  description: string;
  purchaseDate: string;
  notes?: string;
}
