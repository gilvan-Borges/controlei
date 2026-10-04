export type AccountType = 'CHECKING' | 'SAVINGS' | 'CASH' | 'INVESTMENT' | 'OTHER';

export interface Account {
  id: string;
  familyId: string;
  userId: string | null;
  name: string;
  type: AccountType;
  shared: boolean;
  initialBalance: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateAccountRequest {
  name: string;
  type: AccountType;
  shared: boolean;
  userId?: string | null;
  initialBalance: number;
}

export interface UpdateAccountRequest {
  name: string;
  type: AccountType;
  shared: boolean;
  userId?: string | null;
  initialBalance: number;
  active?: boolean;
}
