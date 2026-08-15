export type SplitType = 'EQUAL' | 'PERCENTAGE' | 'EXACT_AMOUNT';

export interface SplitShareItemRequest {
  userId: string;
  amountOrPercentage?: number;
}

export interface CreateSplitRequest {
  transactionId: string;
  splitType: SplitType;
  shares: SplitShareItemRequest[];
  notes?: string;
}

export interface ExpenseSplitShare {
  id: string;
  expenseSplitId: string;
  userId: string;
  userName?: string;
  shareAmount: number;
  settled: boolean;
  settledAt?: string;
}

export interface ExpenseSplit {
  id: string;
  transactionId: string;
  familyId: string;
  paidByUserId: string;
  paidByUserName?: string;
  splitType: SplitType;
  totalAmount: number;
  notes?: string;
  shares: ExpenseSplitShare[];
  createdAt: string;
}

export interface MemberBalance {
  userId: string;
  userName: string;
  totalPaid: number;
  totalOwed: number;
  netBalance: number;
}

export interface SuggestedSettlement {
  fromUserId: string;
  fromUserName: string;
  toUserId: string;
  toUserName: string;
  amount: number;
}

export interface FamilyBalance {
  memberBalances: MemberBalance[];
  suggestedSettlements: SuggestedSettlement[];
}

export interface SettleDebtRequest {
  fromUserId: string;
  toUserId: string;
  amount: number;
  settlementDate?: string;
  notes?: string;
}

export interface SplitSettlement {
  id: string;
  familyId: string;
  fromUserId: string;
  fromUserName?: string;
  toUserId: string;
  toUserName?: string;
  amount: number;
  settlementDate: string;
  notes?: string;
  createdAt: string;
}
