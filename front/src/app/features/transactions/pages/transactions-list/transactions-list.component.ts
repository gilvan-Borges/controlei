import { Component, OnInit } from '@angular/core';
import { TransactionService } from '../../services/transaction.service';
import { UserService } from '../../../../core/services/user.service';
import { CategoryService } from '../../../categories/services/category.service';
import { AccountService } from '../../../accounts/services/account.service';
import { PermissionHelper } from '../../../../core/services/permission.helper';
import { Transaction, TransactionType, TransactionStatus } from '../../../../core/models/transaction.model';
import { User } from '../../../../core/models/user.model';
import { Category } from '../../../../core/models/category.model';
import { Account } from '../../../../core/models/account.model';

@Component({
  selector: 'app-transactions-list',
  standalone: false,
  templateUrl: './transactions-list.component.html',
  styleUrl: './transactions-list.component.scss'
})
export class TransactionsListComponent implements OnInit {
  transactions: Transaction[] = [];
  loading = false;
  errorMessage = '';
  totalElements = 0;
  currentPage = 0;
  pageSize = 20;

  users: User[] = [];
  categories: Category[] = [];
  accounts: Account[] = [];

  filterStartDate = '';
  filterEndDate = '';
  filterUserId = '';
  filterCategoryId = '';
  filterType: TransactionType | '' = '';
  filterStatus: TransactionStatus | '' = '';

  transactionTypes: { value: TransactionType; label: string }[] = [
    { value: 'INCOME', label: 'Receita' },
    { value: 'EXPENSE', label: 'Despesa' },
    { value: 'TRANSFER', label: 'Transferência' }
  ];

  transactionStatuses: { value: TransactionStatus; label: string }[] = [
    { value: 'PENDING', label: 'Pendente' },
    { value: 'PAID', label: 'Pago' },
    { value: 'OVERDUE', label: 'Vencido' },
    { value: 'CANCELED', label: 'Cancelado' }
  ];

  constructor(
    private transactionService: TransactionService,
    private userService: UserService,
    private categoryService: CategoryService,
    private accountService: AccountService,
    public permissions: PermissionHelper
  ) {}

  ngOnInit(): void {
    this.loadFilters();
    this.loadTransactions();
  }

  private loadFilters(): void {
    this.userService.listUsers().subscribe({ next: (u) => this.users = u, error: () => {} });
    this.categoryService.listCategories().subscribe({ next: (c) => this.categories = c, error: () => {} });
    this.accountService.listAccounts().subscribe({ next: (a) => this.accounts = a, error: () => {} });
  }

  loadTransactions(): void {
    this.loading = true;
    this.errorMessage = '';
    const filters: any = {
      page: this.currentPage,
      size: this.pageSize
    };
    if (this.filterStartDate) filters.startDate = this.filterStartDate;
    if (this.filterEndDate) filters.endDate = this.filterEndDate;
    if (this.filterUserId) filters.userId = this.filterUserId;
    if (this.filterCategoryId) filters.categoryId = this.filterCategoryId;
    if (this.filterType) filters.type = this.filterType;
    if (this.filterStatus) filters.status = this.filterStatus;

    this.transactionService.listTransactions(filters).subscribe({
      next: (result) => {
        this.transactions = result.content;
        this.totalElements = result.totalElements;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Erro ao carregar transações.';
        this.loading = false;
      }
    });
  }

  getTypeLabel(type: TransactionType): string {
    return this.transactionTypes.find(t => t.value === type)?.label || type;
  }

  getStatusLabel(status: TransactionStatus): string {
    return this.transactionStatuses.find(s => s.value === status)?.label || status;
  }

  getStatusBadgeClass(status: TransactionStatus): string {
    const map: Record<string, string> = {
      PENDING: 'bg-warning text-dark',
      PAID: 'bg-success',
      OVERDUE: 'bg-danger',
      CANCELED: 'bg-secondary'
    };
    return map[status] || 'bg-secondary';
  }

  getTypeBadgeClass(type: TransactionType): string {
    return type === 'INCOME' ? 'bg-primary' : type === 'EXPENSE' ? 'bg-danger' : 'bg-info';
  }

  canEditTransaction(transaction: Transaction): boolean {
    return this.permissions.canEditRecord(transaction.userId);
  }

  payTransaction(transaction: Transaction): void {
    this.transactionService.payTransaction(transaction.id).subscribe({
      next: () => this.loadTransactions(),
      error: (err) => this.errorMessage = err?.message || 'Erro ao pagar transação.'
    });
  }

  cancelTransaction(transaction: Transaction): void {
    this.transactionService.cancelTransaction(transaction.id).subscribe({
      next: () => this.loadTransactions(),
      error: (err) => this.errorMessage = err?.message || 'Erro ao cancelar transação.'
    });
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.loadTransactions();
  }

  getUserName(userId: string): string {
    return this.users.find(u => u.id === userId)?.name || userId;
  }

  getCategoryName(categoryId: string | null): string {
    if (!categoryId) return '-';
    return this.categories.find(c => c.id === categoryId)?.name || categoryId;
  }

  getAccountName(accountId: string): string {
    return this.accounts.find(a => a.id === accountId)?.name || accountId;
  }

  get pages(): number[] {
    return Array.from({ length: Math.ceil(this.totalElements / this.pageSize) }, (_, index) => index);
  }
}
