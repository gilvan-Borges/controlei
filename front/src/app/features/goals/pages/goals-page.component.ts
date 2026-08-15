import { Component, OnInit } from '@angular/core';
import { GoalService } from '../../../core/services/goal.service';
import { AccountService } from '../../accounts/services/account.service';
import { FinancialGoal, CreateGoalRequest, CreateGoalContributionRequest, WithdrawGoalRequest, GoalCategory } from '../../../core/models/goal.model';
import { Account } from '../../../core/models/account.model';

@Component({
  selector: 'app-goals-page',
  standalone: false,
  templateUrl: './goals-page.component.html',
  styleUrl: './goals-page.component.scss'
})
export class GoalsPageComponent implements OnInit {
  goals: FinancialGoal[] = [];
  accounts: Account[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';

  // Modals
  showCreateModal = false;
  showContributeModal = false;
  showWithdrawModal = false;
  selectedGoal: FinancialGoal | null = null;

  newGoal: CreateGoalRequest = {
    name: '',
    description: '',
    targetAmount: 5000,
    targetDate: '2026-12-31',
    category: 'GENERAL'
  };

  contribution: CreateGoalContributionRequest = {
    amount: 100,
    accountId: '',
    notes: 'Aporte mensal'
  };

  withdrawal: WithdrawGoalRequest = {
    amount: 100,
    accountId: '',
    notes: 'Resgate parcial'
  };

  constructor(
    private goalService: GoalService,
    private accountService: AccountService
  ) {}

  ngOnInit(): void {
    this.loadGoals();
    this.loadAccounts();
  }

  loadGoals(): void {
    this.loading = true;
    this.errorMessage = '';
    this.goalService.listGoals().subscribe({
      next: (data) => {
        this.goals = data;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = 'Erro ao carregar metas: ' + (err.error?.message || err.message);
        this.loading = false;
      }
    });
  }

  loadAccounts(): void {
    this.accountService.listAccounts().subscribe({
      next: (accs) => {
        this.accounts = accs;
        if (accs.length > 0) {
          this.contribution.accountId = accs[0].id;
          this.withdrawal.accountId = accs[0].id;
        }
      }
    });
  }

  openCreateModal(): void {
    this.newGoal = {
      name: '',
      description: '',
      targetAmount: 5000,
      targetDate: '2026-12-31',
      category: 'GENERAL'
    };
    this.showCreateModal = true;
  }

  openContributeModal(goal: FinancialGoal): void {
    this.selectedGoal = goal;
    this.contribution.amount = 100;
    this.showContributeModal = true;
  }

  openWithdrawModal(goal: FinancialGoal): void {
    this.selectedGoal = goal;
    this.withdrawal.amount = Math.min(100, goal.currentAmount);
    this.showWithdrawModal = true;
  }

  saveGoal(): void {
    if (!this.newGoal.name || this.newGoal.targetAmount <= 0) {
      this.errorMessage = 'Preencha o nome da meta e um valor alvo válido.';
      return;
    }

    this.goalService.createGoal(this.newGoal).subscribe({
      next: () => {
        this.successMessage = 'Meta criada com sucesso!';
        this.showCreateModal = false;
        this.loadGoals();
        setTimeout(() => (this.successMessage = ''), 4000);
      },
      error: (err) => {
        this.errorMessage = 'Erro ao salvar meta: ' + (err.error?.message || err.message);
      }
    });
  }

  submitContribution(): void {
    if (!this.selectedGoal || this.contribution.amount <= 0) return;

    this.goalService.addContribution(this.selectedGoal.id, this.contribution).subscribe({
      next: () => {
        this.successMessage = 'Aporte realizado com sucesso!';
        this.showContributeModal = false;
        this.loadGoals();
        setTimeout(() => (this.successMessage = ''), 4000);
      },
      error: (err) => {
        this.errorMessage = 'Erro ao realizar aporte: ' + (err.error?.message || err.message);
      }
    });
  }

  submitWithdrawal(): void {
    if (!this.selectedGoal || this.withdrawal.amount <= 0) return;

    this.goalService.withdraw(this.selectedGoal.id, this.withdrawal).subscribe({
      next: () => {
        this.successMessage = 'Resgate realizado com sucesso!';
        this.showWithdrawModal = false;
        this.loadGoals();
        setTimeout(() => (this.successMessage = ''), 4000);
      },
      error: (err) => {
        this.errorMessage = 'Erro ao resgatar meta: ' + (err.error?.message || err.message);
      }
    });
  }

  deleteGoal(id: string): void {
    if (confirm('Deseja realmente excluir esta meta financeira?')) {
      this.goalService.deleteGoal(id).subscribe({
        next: () => this.loadGoals()
      });
    }
  }

  getTotalSaved(): number {
    return this.goals.reduce((acc, g) => acc + (g.currentAmount || 0), 0);
  }

  getTotalTarget(): number {
    return this.goals.reduce((acc, g) => acc + (g.targetAmount || 0), 0);
  }

  getCategoryIcon(cat: GoalCategory): string {
    switch (cat) {
      case 'TRAVEL': return 'bi-airplane';
      case 'VEHICLE': return 'bi-car-front';
      case 'REAL_ESTATE': return 'bi-house';
      case 'EDUCATION': return 'bi-mortarboard';
      case 'EMERGENCY_FUND': return 'bi-shield-check';
      case 'ELECTRONICS': return 'bi-laptop';
      default: return 'bi-piggy-bank';
    }
  }
}
