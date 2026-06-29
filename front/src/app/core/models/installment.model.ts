export type InstallmentStatus = 'PENDING' | 'PAID' | 'CANCELED';

export interface Installment {
  id: string;
  familyId: string;
  userId: string;
  debtId: string;
  installmentNumber: number;
  amount: number;
  dueDate: string;
  paidAt: string | null;
  status: InstallmentStatus;
  createdAt: string;
  updatedAt: string;
}

export interface PayInstallmentResponse {
  installment: Installment;
  debt: import('./debt.model').Debt;
}
