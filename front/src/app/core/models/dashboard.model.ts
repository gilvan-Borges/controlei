export interface UpcomingInstallment {
  id: string;
  debtId: string;
  debtDescription: string;
  installmentNumber: number;
  amount: number;
  dueDate: string;
}

export interface UserDashboardDetail {
  userId: string;
  userName: string;
  income: number;
  expense: number;
  balance: number;
  openDebts: number;
  pendingInstallments: number;
  investments: number;
}

export interface IndividualDashboardResponse {
  totalIncome: number;
  totalExpense: number;
  balance: number;
  totalOpenDebts: number;
  totalPendingInstallments: number;
  upcomingInstallments: UpcomingInstallment[];
  totalInvested: number;
}

export interface FamilyDashboardResponse {
  totalIncome: number;
  totalExpense: number;
  balance: number;
  totalOpenDebts: number;
  totalPendingInstallments: number;
  totalInvested: number;
  userDetails: UserDashboardDetail[];
}
