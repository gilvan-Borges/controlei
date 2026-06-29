import { Component, OnInit } from '@angular/core';
import { InvestmentService } from '../../services/investment.service';
import { UserService } from '../../../../core/services/user.service';
import { CategoryService } from '../../../categories/services/category.service';
import { PermissionHelper } from '../../../../core/services/permission.helper';
import { Investment, InvestmentType } from '../../../../core/models/investment.model';
import { User } from '../../../../core/models/user.model';
import { Category } from '../../../../core/models/category.model';

@Component({
  selector: 'app-investments-list',
  standalone: false,
  templateUrl: './investments-list.component.html',
  styleUrl: './investments-list.component.scss'
})
export class InvestmentsListComponent implements OnInit {
  investments: Investment[] = [];
  users: User[] = [];
  categories: Category[] = [];
  loading = false;
  errorMessage = '';
  showDeleteConfirm = false;
  investmentToDelete: Investment | null = null;

  filterUserId = '';
  filterCategoryId = '';
  filterType: InvestmentType | '' = '';

  investmentTypes: { value: InvestmentType; label: string }[] = [
    { value: 'SAVINGS', label: 'Poupanca' },
    { value: 'FIXED_INCOME', label: 'Renda fixa' },
    { value: 'STOCK', label: 'Acao' },
    { value: 'FUND', label: 'Fundo' },
    { value: 'CRYPTO', label: 'Cripto' },
    { value: 'OTHER', label: 'Outro' }
  ];

  constructor(
    private investmentService: InvestmentService,
    private userService: UserService,
    private categoryService: CategoryService,
    public permissions: PermissionHelper
  ) {}

  ngOnInit(): void {
    this.userService.listUsers().subscribe({ next: users => this.users = users, error: () => {} });
    this.categoryService.listCategories({ type: 'INVESTMENT' }).subscribe({ next: categories => this.categories = categories, error: () => {} });
    this.loadInvestments();
  }

  loadInvestments(): void {
    this.loading = true;
    this.errorMessage = '';
    this.investmentService.listInvestments({
      userId: this.filterUserId || undefined,
      categoryId: this.filterCategoryId || undefined,
      type: this.filterType || undefined
    }).subscribe({
      next: investments => {
        this.investments = investments;
        this.loading = false;
      },
      error: err => {
        this.errorMessage = err?.message || 'Erro ao carregar investimentos.';
        this.loading = false;
      }
    });
  }

  onFilterChange(): void {
    this.loadInvestments();
  }

  confirmDelete(investment: Investment): void {
    this.investmentToDelete = investment;
    this.showDeleteConfirm = true;
  }

  onDeleteConfirm(): void {
    if (!this.investmentToDelete) {
      return;
    }

    this.investmentService.deleteInvestment(this.investmentToDelete.id).subscribe({
      next: () => {
        this.showDeleteConfirm = false;
        this.investmentToDelete = null;
        this.loadInvestments();
      },
      error: err => {
        this.showDeleteConfirm = false;
        this.errorMessage = err?.message || 'Erro ao excluir investimento.';
      }
    });
  }

  onDeleteCancel(): void {
    this.showDeleteConfirm = false;
    this.investmentToDelete = null;
  }

  getTypeLabel(type: InvestmentType): string {
    return this.investmentTypes.find(item => item.value === type)?.label || type;
  }

  getUserName(userId: string): string {
    return this.users.find(user => user.id === userId)?.name || userId;
  }

  getCategoryName(categoryId: string | null): string {
    if (!categoryId) {
      return '-';
    }
    return this.categories.find(category => category.id === categoryId)?.name || categoryId;
  }

  canEditInvestment(investment: Investment): boolean {
    return this.permissions.canEditRecord(investment.userId);
  }
}
