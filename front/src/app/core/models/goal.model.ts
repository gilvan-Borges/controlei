export type GoalCategory =
  | 'TRAVEL'
  | 'EMERGENCY_FUND'
  | 'VEHICLE'
  | 'REAL_ESTATE'
  | 'EDUCATION'
  | 'ELECTRONICS'
  | 'GENERAL';

export type GoalStatus = 'IN_PROGRESS' | 'COMPLETED' | 'PAUSED' | 'CANCELLED';

export interface FinancialGoal {
  id: string;
  familyId: string;
  userId: string;
  userName?: string;
  name: string;
  description?: string;
  targetAmount: number;
  currentAmount: number;
  remainingAmount: number;
  progressPercentage: number;
  targetDate?: string;
  category: GoalCategory;
  status: GoalStatus;
  createdAt: string;
  updatedAt?: string;
}

export interface CreateGoalRequest {
  name: string;
  description?: string;
  targetAmount: number;
  targetDate?: string;
  category?: GoalCategory;
}

export interface UpdateGoalRequest {
  name: string;
  description?: string;
  targetAmount: number;
  targetDate?: string;
  category?: GoalCategory;
  status?: GoalStatus;
}

export interface CreateGoalContributionRequest {
  accountId?: string;
  amount: number;
  contributionDate?: string;
  notes?: string;
}

export interface GoalContribution {
  id: string;
  goalId: string;
  familyId: string;
  userId: string;
  userName?: string;
  accountId?: string;
  accountName?: string;
  amount: number;
  contributionDate: string;
  notes?: string;
  createdAt: string;
}

export interface WithdrawGoalRequest {
  accountId: string;
  amount: number;
  notes?: string;
}
