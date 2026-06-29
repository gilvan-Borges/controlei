import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DebtService } from '../../services/debt.service';
import { InstallmentService } from '../../../installments/services/installment.service';
import { PermissionHelper } from '../../../../core/services/permission.helper';
import { Debt } from '../../../../core/models/debt.model';
import { Installment, InstallmentStatus } from '../../../../core/models/installment.model';

@Component({
  selector: 'app-debt-detail',
  standalone: false,
  templateUrl: './debt-detail.component.html',
  styleUrl: './debt-detail.component.scss'
})
export class DebtDetailComponent implements OnInit {
  debt: Debt | null = null;
  installments: Installment[] = [];
  loading = false;
  errorMessage = '';

  installmentStatuses: { value: InstallmentStatus; label: string }[] = [
    { value: 'PENDING', label: 'Pendente' },
    { value: 'PAID', label: 'Pago' },
    { value: 'CANCELED', label: 'Cancelado' }
  ];

  constructor(
    private route: ActivatedRoute,
    private debtService: DebtService,
    private installmentService: InstallmentService,
    public permissions: PermissionHelper
  ) {}

  ngOnInit(): void {
    const debtId = this.route.snapshot.paramMap.get('id')!;
    this.loadDebt(debtId);
    this.loadInstallments(debtId);
  }

  private loadDebt(id: string): void {
    this.loading = true;
    this.debtService.getDebt(id).subscribe({
      next: (debt) => {
        this.debt = debt;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Erro ao carregar dívida.';
        this.loading = false;
      }
    });
  }

  private loadInstallments(debtId: string): void {
    this.debtService.listInstallmentsByDebt(debtId).subscribe({
      next: (installments) => this.installments = installments,
      error: (err) => this.errorMessage = err?.message || 'Erro ao carregar parcelas.'
    });
  }

  getStatusLabel(status: InstallmentStatus): string {
    return this.installmentStatuses.find(s => s.value === status)?.label || status;
  }

  getStatusBadgeClass(status: InstallmentStatus): string {
    const map: Record<string, string> = {
      PENDING: 'bg-warning text-dark',
      PAID: 'bg-success',
      CANCELED: 'bg-secondary'
    };
    return map[status] || 'bg-secondary';
  }

  getDebtStatusBadgeClass(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'bg-warning text-dark',
      PAID: 'bg-success',
      CANCELED: 'bg-secondary'
    };
    return map[status] || 'bg-secondary';
  }

  payInstallment(installment: Installment): void {
    this.installmentService.payInstallment(installment.id).subscribe({
      next: (response) => {
        this.installments = this.installments.map(i =>
          i.id === response.installment.id ? response.installment : i
        );
        if (response.debt) {
          this.debt = response.debt;
        }
      },
      error: (err) => this.errorMessage = err?.message || 'Erro ao pagar parcela.'
    });
  }

  cancelInstallment(installment: Installment): void {
    this.installmentService.cancelInstallment(installment.id).subscribe({
      next: (response) => {
        this.installments = this.installments.map(i =>
          i.id === response.installment.id ? response.installment : i
        );
        if (response.debt) {
          this.debt = response.debt;
        }
      },
      error: (err) => this.errorMessage = err?.message || 'Erro ao cancelar parcela.'
    });
  }

  canEditInstallment(installment: Installment): boolean {
    return this.permissions.canEditRecord(installment.userId);
  }
}
