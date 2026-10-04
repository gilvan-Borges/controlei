export type BudgetStatus = 'NORMAL' | 'WARNING' | 'EXCEEDED';

export interface Budget {
  id: string;
  familyId: string;
  userId?: string;
  userName?: string;
  categoryId: string;
  categoryName?: string;
  categoryColor?: string;
  categoryIcon?: string;
  year: number;
  month: number;
  plannedAmount: number;
  spentAmount: number;
  remainingAmount: number;
  percentageUsed: number;
  alertThresholdPercent: number;
  status: BudgetStatus;
  createdAt: string;
  updatedAt?: string;
}

export interface CreateBudgetRequest {
  userId?: string;
  categoryId: string;
  year: number;
  month: number;
  plannedAmount: number;
  alertThresholdPercent?: number;
}

export interface UpdateBudgetRequest {
  plannedAmount: number;
  alertThresholdPercent?: number;
}

export interface BudgetSummary {
  year: number;
  month: number;
  totalPlanned: number;
  totalSpent: number;
  totalRemaining: number;
  overallPercentageUsed: number;
  items: Budget[];
}
