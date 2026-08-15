export interface AccountTaxItem {
  accountName: string;
  accountType: string;
  balance: number;
}

export interface InvestmentTaxItem {
  investmentName: string;
  investmentType: string;
  currentAmount: number;
}

export interface DebtTaxItem {
  debtName: string;
  totalAmount: number;
  remainingAmount: number;
}

export interface TaxDeclarationReport {
  year: number;
  familyName: string;
  accounts: AccountTaxItem[];
  investments: InvestmentTaxItem[];
  debts: DebtTaxItem[];
  totalAssets: number;
  totalLiabilities: number;
  netWorth: number;
}
