import { Component, OnInit } from '@angular/core';
import { RecurringTransactionService } from '../../../core/services/recurring-transaction.service';
import { AccountService } from '../../accounts/services/account.service';
import { CategoryService } from '../../categories/services/category.service';
import { RecurringTransaction, CreateRecurringTransactionRequest } from '../../../core/models/recurring-transaction.model';
import { Account } from '../../../core/models/account.model';
import { Category } from '../../../core/models/category.model';

@Component({
  selector: 'app-recurring-page',
  standalone: false,
  templateUrl: './recurring-page.component.html',
  styleUrl: './recurring-page.component.scss'
})
export class RecurringPageComponent implements OnInit {
  items: RecurringTransaction[] = [];
  accounts: Account[] = [];
  categories: Category[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';

  showCreateModal = false;
  newItem: CreateRecurringTransactionRequest = {
    accountId: '',
    categoryId: '',
    type: 'EXPENSE',
    description: 'Netflix / Spotify / Aluguel',
    amount: 55.90,
    frequency: 'MONTHLY',
    dayOfMonth: 10,
    startDate: new Date().toISOString().split('T')[0],
    autoPay: true
  };

  constructor(
    private recurringService: RecurringTransactionService,
    private accountService: AccountService,
    private categoryService: CategoryService
  ) {}

  ngOnInit(): void {
    this.loadData();
    this.loadAccounts();
    this.loadCategories();
  }

  loadData(): void {
    this.loading = true;
    this.errorMessage = '';
    this.recurringService.list().subscribe({
      next: (data) => {
        this.items = data;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = 'Erro ao carregar assinaturas recorrentes: ' + (err.error?.message || err.message);
        this.loading = false;
      }
    });
  }

  loadAccounts(): void {
    this.accountService.listAccounts().subscribe({
      next: (accs) => {
        this.accounts = accs;
        if (accs.length > 0 && !this.newItem.accountId) {
          this.newItem.accountId = accs[0].id;
        }
      }
    });
  }

  loadCategories(): void {
    this.categoryService.listCategories().subscribe({
      next: (cats) => {
        this.categories = cats;
        if (cats.length > 0 && !this.newItem.categoryId) {
          this.newItem.categoryId = cats[0].id;
        }
      }
    });
  }

  openCreateModal(): void {
    this.newItem = {
      accountId: this.accounts.length > 0 ? this.accounts[0].id : '',
      categoryId: this.categories.length > 0 ? this.categories[0].id : '',
      type: 'EXPENSE',
      description: '',
      amount: 50,
      frequency: 'MONTHLY',
      dayOfMonth: 5,
      startDate: new Date().toISOString().split('T')[0],
      autoPay: true
    };
    this.showCreateModal = true;
  }

  saveItem(): void {
    if (!this.newItem.description || this.newItem.amount <= 0 || !this.newItem.accountId) {
      this.errorMessage = 'Preencha a descrição, valor e selecione a conta.';
      return;
    }

    this.recurringService.create(this.newItem).subscribe({
      next: () => {
        this.successMessage = 'Transação recorrente cadastrada com sucesso!';
        this.showCreateModal = false;
        this.loadData();
        setTimeout(() => (this.successMessage = ''), 4000);
      },
      error: (err) => {
        this.errorMessage = 'Erro ao cadastrar recorrente: ' + (err.error?.message || err.message);
      }
    });
  }

  toggleStatus(item: RecurringTransaction): void {
    this.recurringService.toggleActive(item.id).subscribe({
      next: () => this.loadData()
    });
  }

  deleteItem(id: string): void {
    if (confirm('Deseja excluir esta assinatura/recorrência?')) {
      this.recurringService.delete(id).subscribe({
        next: () => this.loadData()
      });
    }
  }

  getTotalMonthlyExpenses(): number {
    return this.items
      .filter((i) => i.active && i.type === 'EXPENSE')
      .reduce((acc, i) => acc + (i.amount || 0), 0);
  }
}
