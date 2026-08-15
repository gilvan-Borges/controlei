import { Component, OnInit } from '@angular/core';
import { SplitService } from '../../../core/services/split.service';
import { UserService } from '../../../core/services/user.service';
import { ExpenseSplit, FamilyBalance, SuggestedSettlement, SettleDebtRequest } from '../../../core/models/split.model';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-splits-page',
  standalone: false,
  templateUrl: './splits-page.component.html',
  styleUrl: './splits-page.component.scss'
})
export class SplitsPageComponent implements OnInit {
  splits: ExpenseSplit[] = [];
  familyBalance: FamilyBalance | null = null;
  users: User[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';

  showSettleModal = false;
  settleRequest: SettleDebtRequest = {
    fromUserId: '',
    toUserId: '',
    amount: 0,
    settlementDate: new Date().toISOString().split('T')[0],
    notes: 'Quitação de despesas divididas'
  };

  constructor(
    private splitService: SplitService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.loadData();
    this.loadUsers();
  }

  loadData(): void {
    this.loading = true;
    this.errorMessage = '';
    this.splitService.listSplits().subscribe({
      next: (data) => {
        this.splits = data;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = 'Erro ao carregar divisões: ' + (err.error?.message || err.message);
        this.loading = false;
      }
    });

    this.splitService.getBalances().subscribe({
      next: (bal) => (this.familyBalance = bal),
      error: () => {}
    });
  }

  loadUsers(): void {
    this.userService.listUsers().subscribe({
      next: (data) => (this.users = data)
    });
  }

  openSettleModal(suggested?: SuggestedSettlement): void {
    if (suggested) {
      this.settleRequest = {
        fromUserId: suggested.fromUserId,
        toUserId: suggested.toUserId,
        amount: suggested.amount,
        settlementDate: new Date().toISOString().split('T')[0],
        notes: `Acerto direto: ${suggested.fromUserName} paga ${suggested.toUserName}`
      };
    } else {
      this.settleRequest = {
        fromUserId: this.users.length > 0 ? this.users[0].id : '',
        toUserId: this.users.length > 1 ? this.users[1].id : (this.users[0]?.id || ''),
        amount: 50,
        settlementDate: new Date().toISOString().split('T')[0],
        notes: 'Acerto de contas familiar'
      };
    }
    this.showSettleModal = true;
  }

  submitSettlement(): void {
    if (!this.settleRequest.fromUserId || !this.settleRequest.toUserId || this.settleRequest.amount <= 0) {
      this.errorMessage = 'Preencha todos os campos do acerto.';
      return;
    }

    this.splitService.settleDebt(this.settleRequest).subscribe({
      next: () => {
        this.successMessage = 'Acerto registrado e saldo quitado com sucesso!';
        this.showSettleModal = false;
        this.loadData();
        setTimeout(() => (this.successMessage = ''), 4000);
      },
      error: (err) => {
        this.errorMessage = 'Erro ao realizar acerto: ' + (err.error?.message || err.message);
      }
    });
  }
}
