export type InvestmentType =
  | 'SAVINGS'
  | 'FIXED_INCOME'
  | 'STOCK'
  | 'FUND'
  | 'REAL_ESTATE_FUND'
  | 'CRYPTO'
  | 'OTHER';

export type InvestmentTransactionType = 'BUY' | 'SELL' | 'DIVIDEND' | 'INTEREST' | 'AMORTIZATION';

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

export interface CreateInvestmentTransactionRequest {
  accountId?: string;
  type: InvestmentTransactionType;
  quantity?: number;
  unitPrice?: number;
  totalAmount: number;
  transactionDate?: string;
  notes?: string;
}

export interface InvestmentTransaction {
  id: string;
  investmentId: string;
  familyId: string;
  userId: string;
  userName?: string;
  accountId?: string;
  accountName?: string;
  type: InvestmentTransactionType;
  quantity?: number;
  unitPrice?: number;
  totalAmount: number;
  transactionDate: string;
  notes?: string;
  createdAt: string;
}

export interface InvestmentAssetClassSummary {
  type: InvestmentType;
  totalAmount: number;
  percentage: number;
  count: number;
}

export interface InvestmentPortfolioSummary {
  totalPortfolioValue: number;
  totalInvestmentsCount: number;
  assetClasses: InvestmentAssetClassSummary[];
}
