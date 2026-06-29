export type InvestmentType = 'SAVINGS' | 'FIXED_INCOME' | 'STOCK' | 'FUND' | 'CRYPTO' | 'OTHER';

export interface Investment {
  id: string;
  familyId: string;
  userId: string;
  categoryId: string | null;
  name: string;
  type: InvestmentType;
  currentAmount: number;
  referenceDate: string | null;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateInvestmentRequest {
  userId: string;
  categoryId?: string | null;
  name: string;
  type: InvestmentType;
  currentAmount: number;
  referenceDate?: string | null;
  notes?: string;
}

export interface UpdateInvestmentRequest {
  categoryId?: string | null;
  name: string;
  type: InvestmentType;
  currentAmount: number;
  referenceDate?: string | null;
  notes?: string;
}
