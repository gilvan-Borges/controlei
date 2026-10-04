import { Component, OnInit } from '@angular/core';
import { DebtService } from '../../services/debt.service';
import { UserService } from '../../../../core/services/user.service';
import { PermissionHelper } from '../../../../core/services/permission.helper';
import { Debt, DebtStatus } from '../../../../core/models/debt.model';
import { User } from '../../../../core/models/user.model';

@Component({
  selector: 'app-debts-list',
  standalone: false,
  templateUrl: './debts-list.component.html',
  styleUrl: './debts-list.component.scss'
})
export class DebtsListComponent implements OnInit {
  debts: Debt[] = [];
  loading = false;
  errorMessage = '';
  showDeleteConfirm = false;
  debtToDelete: Debt | null = null;

  users: User[] = [];
  filterUserId = '';
  filterStatus: DebtStatus | '' = '';

  debtStatuses: { value: DebtStatus; label: string }[] = [
    { value: 'PENDING', label: 'Pendente' },
    { value: 'PAID', label: 'Pago' },
    { value: 'CANCELED', label: 'Cancelado' }
  ];

  constructor(
    private debtService: DebtService,
    private userService: UserService,
    public permissions: PermissionHelper
  ) {}

  ngOnInit(): void {
    this.userService.listUsers().subscribe({ next: (u) => this.users = u, error: () => {} });
    this.loadDebts();
  }

  loadDebts(): void {
    this.loading = true;
    this.errorMessage = '';
    const filters: any = {};
    if (this.filterUserId) filters.userId = this.filterUserId;
    if (this.filterStatus) filters.status = this.filterStatus;

    this.debtService.listDebts(filters).subscribe({
      next: (debts) => {
        this.debts = debts;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Erro ao carregar dívidas.';
        this.loading = false;
      }
    });
  }

  getStatusLabel(status: DebtStatus): string {
    return this.debtStatuses.find(s => s.value === status)?.label || status;
  }

  getStatusBadgeClass(status: DebtStatus): string {
    const map: Record<string, string> = {
      PENDING: 'bg-warning text-dark',
      PAID: 'bg-success',
      CANCELED: 'bg-secondary'
    };
    return map[status] || 'bg-secondary';
  }

  canEditDebt(debt: Debt): boolean {
    return this.permissions.canEditRecord(debt.userId);
  }

  confirmDelete(debt: Debt): void {
    this.debtToDelete = debt;
    this.showDeleteConfirm = true;
  }

  onDeleteConfirm(): void {
    if (!this.debtToDelete) return;
    this.debtService.deleteDebt(this.debtToDelete.id).subscribe({
      next: () => {
        this.showDeleteConfirm = false;
        this.debtToDelete = null;
        this.loadDebts();
      },
      error: (err) => {
        this.showDeleteConfirm = false;
        this.errorMessage = err?.message || 'Erro ao excluir dívida.';
      }
    });
  }

  onDeleteCancel(): void {
    this.showDeleteConfirm = false;
    this.debtToDelete = null;
  }

  onFilterChange(): void {
    this.loadDebts();
  }

  getUserName(userId: string): string {
    return this.users.find(u => u.id === userId)?.name || userId;
  }
}
