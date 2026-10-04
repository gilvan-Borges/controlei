import { Component, OnInit } from '@angular/core';
import { OpenFinanceService } from '../../../core/services/open-finance.service';
import { AccountService } from '../../accounts/services/account.service';
import { BankConnection, ConnectBankRequest, SyncTransactionsResponse } from '../../../core/models/open-finance.model';
import { Account } from '../../../core/models/account.model';

@Component({
  selector: 'app-open-finance-page',
  standalone: false,
  templateUrl: './open-finance-page.component.html',
  styleUrl: './open-finance-page.component.scss'
})
export class OpenFinancePageComponent implements OnInit {
  connections: BankConnection[] = [];
  accounts: Account[] = [];
  loading = false;
  syncingId: string | null = null;
  errorMessage = '';
  successMessage = '';

  showConnectModal = false;
  selectedBank = {
    id: 'nubank',
    name: 'Nubank',
    color: '#820ad1',
    logo: 'bi-gem'
  };

  availableBanks = [
    { id: 'nubank', name: 'Nubank', color: '#820ad1', icon: 'bi-gem' },
    { id: 'itau', name: 'Itaú Unibanco', color: '#ec7000', icon: 'bi-bank' },
    { id: 'bradesco', name: 'Bradesco', color: '#cc092f', icon: 'bi-building' },
    { id: 'inter', name: 'Banco Inter', color: '#ff7a00', icon: 'bi-wallet' },
    { id: 'santander', name: 'Santander', color: '#ea1d25', icon: 'bi-credit-card' },
    { id: 'bb', name: 'Banco do Brasil', color: '#fcee21', icon: 'bi-cash' },
    { id: 'caixa', name: 'Caixa Econômica', color: '#005ca9', icon: 'bi-house' }
  ];

  connectRequest: ConnectBankRequest = {
    institutionId: 'nubank',
    institutionName: 'Nubank',
    externalItemId: 'item_pluggy_' + Math.random().toString(36).substring(7),
    targetAccountId: ''
  };

  constructor(
    private openFinanceService: OpenFinanceService,
    private accountService: AccountService
  ) {}

  ngOnInit(): void {
    this.loadConnections();
    this.loadAccounts();
  }

  loadConnections(): void {
    this.loading = true;
    this.errorMessage = '';
    this.openFinanceService.listConnections().subscribe({
      next: (data) => {
        this.connections = data;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = 'Erro ao carregar conexões bancárias: ' + (err.error?.message || err.message);
        this.loading = false;
      }
    });
  }

  loadAccounts(): void {
    this.accountService.listAccounts().subscribe({
      next: (accs) => {
        this.accounts = accs;
        if (accs.length > 0) {
          this.connectRequest.targetAccountId = accs[0].id;
        }
      }
    });
  }

  openModal(bank: any): void {
    this.selectedBank = bank;
    this.connectRequest = {
      institutionId: bank.id,
      institutionName: bank.name,
      externalItemId: 'item_' + bank.id + '_' + Math.random().toString(36).substring(7),
      targetAccountId: this.accounts.length > 0 ? this.accounts[0].id : ''
    };
    this.showConnectModal = true;
  }

  submitConnect(): void {
    this.openFinanceService.connectBank(this.connectRequest).subscribe({
      next: () => {
        this.successMessage = `Conexão com ${this.selectedBank.name} autorizada via Open Finance!`;
        this.showConnectModal = false;
        this.loadConnections();
        setTimeout(() => (this.successMessage = ''), 5000);
      },
      error: (err) => {
        this.errorMessage = 'Erro ao conectar instituição: ' + (err.error?.message || err.message);
      }
    });
  }

  sync(connection: BankConnection): void {
    this.syncingId = connection.id;
    this.openFinanceService.syncConnection(connection.id).subscribe({
      next: (res: SyncTransactionsResponse) => {
        this.syncingId = null;
        this.successMessage = `${res.message} (${res.newTransactionsImported} novas transações importadas, ${res.duplicatesSkipped} duplicatas ignoradas).`;
        this.loadConnections();
        setTimeout(() => (this.successMessage = ''), 5000);
      },
      error: (err) => {
        this.syncingId = null;
        this.errorMessage = 'Erro na sincronização: ' + (err.error?.message || err.message);
      }
    });
  }

  disconnect(id: string): void {
    if (confirm('Deseja realmente revogar o consentimento desta conexão bancária?')) {
      this.openFinanceService.disconnect(id).subscribe({
        next: () => this.loadConnections()
      });
    }
  }
}
