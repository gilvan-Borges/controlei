import { Component, OnInit } from '@angular/core';
import { InstallmentService } from '../../services/installment.service';
import { UserService } from '../../../../core/services/user.service';
import { Installment, InstallmentStatus } from '../../../../core/models/installment.model';
import { User } from '../../../../core/models/user.model';

@Component({
  selector: 'app-installments-list',
  standalone: false,
  templateUrl: './installments-list.component.html',
  styleUrl: './installments-list.component.scss'
})
export class InstallmentsListComponent implements OnInit {
  installments: Installment[] = [];
  users: User[] = [];
  loading = false;
  errorMessage = '';

  filterUserId = '';
  filterStatus: InstallmentStatus | '' = '';
  filterStartDate = '';
  filterEndDate = '';

  statuses: { value: InstallmentStatus; label: string }[] = [
    { value: 'PENDING', label: 'Pendente' },
    { value: 'PAID', label: 'Pago' },
    { value: 'CANCELED', label: 'Cancelado' }
  ];

  constructor(
    private installmentService: InstallmentService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.userService.listUsers().subscribe({ next: users => this.users = users, error: () => {} });
    this.loadInstallments();
  }

  loadInstallments(): void {
    this.loading = true;
    this.errorMessage = '';
    this.installmentService.listInstallments({
      userId: this.filterUserId || undefined,
      status: this.filterStatus || undefined,
      startDate: this.filterStartDate || undefined,
      endDate: this.filterEndDate || undefined
    }).subscribe({
      next: installments => {
        this.installments = installments;
        this.loading = false;
      },
      error: err => {
        this.errorMessage = err?.message || 'Erro ao carregar parcelas.';
        this.loading = false;
      }
    });
  }

  onFilterChange(): void {
    this.loadInstallments();
  }

  pay(installment: Installment): void {
    this.installmentService.payInstallment(installment.id).subscribe({
      next: () => this.loadInstallments(),
      error: err => this.errorMessage = err?.message || 'Erro ao pagar parcela.'
    });
  }

  cancel(installment: Installment): void {
    this.installmentService.cancelInstallment(installment.id).subscribe({
      next: () => this.loadInstallments(),
      error: err => this.errorMessage = err?.message || 'Erro ao cancelar parcela.'
    });
  }

  getStatusLabel(status: InstallmentStatus): string {
    return this.statuses.find(item => item.value === status)?.label || status;
  }

  getStatusBadgeClass(status: InstallmentStatus): string {
    const map: Record<InstallmentStatus, string> = {
      PENDING: 'bg-warning text-dark',
      PAID: 'bg-success',
      CANCELED: 'bg-secondary'
    };
    return map[status];
  }

  getUserName(userId: string): string {
    return this.users.find(user => user.id === userId)?.name || userId;
  }
}
