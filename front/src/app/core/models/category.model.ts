export type CategoryType = 'INCOME' | 'EXPENSE' | 'DEBT' | 'INVESTMENT';

export interface Category {
  id: string;
  familyId: string;
  name: string;
  type: CategoryType;
  color: string | null;
  icon: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateCategoryRequest {
  name: string;
  type: CategoryType;
  color?: string;
  icon?: string;
}

export interface UpdateCategoryRequest {
  name: string;
  type: CategoryType;
  color?: string;
  icon?: string;
  active?: boolean;
}
