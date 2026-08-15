export interface CreditCard {
  id: string;
  familyId: string;
  userId: string;
  userName?: string;
  name: string;
  lastDigits?: string;
  brand?: string;
  closingDay: number;
  dueDay: number;
  creditLimit: number;
  availableLimit: number;
  currentInvoiceAmount: number;
  active: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface CreateCreditCardRequest {
  userId?: string;
  name: string;
  lastDigits?: string;
  brand?: string;
  closingDay: number;
  dueDay: number;
  creditLimit: number;
}

export interface UpdateCreditCardRequest {
  name: string;
  lastDigits?: string;
  brand?: string;
  closingDay: number;
  dueDay: number;
  creditLimit: number;
  active?: boolean;
}

export interface CreateCardExpenseRequest {
  description: string;
  amount: number;
  transactionDate: string;
  categoryId?: string;
  totalInstallments?: number;
}

export type InvoiceStatus = 'OPEN' | 'CLOSED' | 'PAID' | 'OVERDUE';

export interface CreditCardTransaction {
  id: string;
  creditCardId: string;
  invoiceId: string;
  familyId: string;
  userId: string;
  categoryId?: string;
  categoryName?: string;
  description: string;
  amount: number;
  transactionDate: string;
  installmentNumber: number;
  totalInstallments: number;
  createdAt: string;
}

export interface Invoice {
  id: string;
  creditCardId: string;
  creditCardName: string;
  familyId: string;
  userId: string;
  userName?: string;
  referenceMonth: string;
  totalAmount: number;
  paidAmount: number;
  status: InvoiceStatus;
  dueDate: string;
  paidAt?: string;
  transactions: CreditCardTransaction[];
  createdAt: string;
}

export interface PayInvoiceRequest {
  accountId: string;
  amount?: number;
  paymentDate?: string;
}
