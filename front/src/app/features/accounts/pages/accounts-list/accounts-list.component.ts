import { Component, OnInit } from '@angular/core';
import { AccountService } from '../../services/account.service';
import { PermissionHelper } from '../../../../core/services/permission.helper';
import { Account, AccountType } from '../../../../core/models/account.model';

@Component({
  selector: 'app-accounts-list',
  standalone: false,
  templateUrl: './accounts-list.component.html',
  styleUrl: './accounts-list.component.scss'
})
export class AccountsListComponent implements OnInit {
  accounts: Account[] = [];
  loading = false;
  errorMessage = '';
  showDeleteConfirm = false;
  accountToDelete: Account | null = null;

  filterType: AccountType | '' = '';
  filterShared: string = '';

  accountTypes: { value: AccountType; label: string }[] = [
    { value: 'CHECKING', label: 'Corrente' },
    { value: 'SAVINGS', label: 'Poupança' },
    { value: 'CASH', label: 'Dinheiro' },
    { value: 'INVESTMENT', label: 'Investimento' },
    { value: 'OTHER', label: 'Outro' }
  ];

  constructor(
    private accountService: AccountService,
    public permissions: PermissionHelper
  ) {}

  ngOnInit(): void {
    this.loadAccounts();
  }

  loadAccounts(): void {
    this.loading = true;
    this.errorMessage = '';
    const filters: any = {};
    if (this.filterType) filters.type = this.filterType;
    if (this.filterShared === 'true') filters.shared = true;
    if (this.filterShared === 'false') filters.shared = false;

    this.accountService.listAccounts(filters).subscribe({
      next: (accounts) => {
        this.accounts = accounts;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Erro ao carregar contas.';
        this.loading = false;
      }
    });
  }

  getTypeLabel(type: AccountType): string {
    return this.accountTypes.find(t => t.value === type)?.label || type;
  }

  confirmDelete(account: Account): void {
    this.accountToDelete = account;
    this.showDeleteConfirm = true;
  }

  onDeleteConfirm(): void {
    if (!this.accountToDelete) return;
    this.accountService.deleteAccount(this.accountToDelete.id).subscribe({
      next: () => {
        this.showDeleteConfirm = false;
        this.accountToDelete = null;
        this.loadAccounts();
      },
      error: (err) => {
        this.showDeleteConfirm = false;
        this.errorMessage = err?.message || 'Erro ao excluir conta.';
      }
    });
  }

  onDeleteCancel(): void {
    this.showDeleteConfirm = false;
    this.accountToDelete = null;
  }

  onFilterChange(): void {
    this.loadAccounts();
  }
}
