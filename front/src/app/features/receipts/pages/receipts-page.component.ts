import { Component, OnInit } from '@angular/core';
import { ReceiptService } from '../../../core/services/receipt.service';
import { TransactionService } from '../../transactions/services/transaction.service';
import { AccountService } from '../../accounts/services/account.service';
import { CategoryService } from '../../categories/services/category.service';
import { ReceiptScan } from '../../../core/models/receipt.model';
import { Account } from '../../../core/models/account.model';
import { Category } from '../../../core/models/category.model';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-receipts-page',
  standalone: false,
  templateUrl: './receipts-page.component.html',
  styleUrl: './receipts-page.component.scss'
})
export class ReceiptsPageComponent implements OnInit {
  scans: ReceiptScan[] = [];
  accounts: Account[] = [];
  categories: Category[] = [];
  loading = false;
  scanning = false;
  errorMessage = '';
  successMessage = '';

  simulatedText = `SUPERMERCADO PAO DE ACUCAR LTDA\nCNPJ: 47.508.411/0001-56\nDATA: 15/08/2026 14:32\n\n1. CAFE TORRADO 500G - R$ 18,90\n2. LEITE INTEGRAL 1L - R$ 6,50\n3. QUEIJO MUSSARELA 400G - R$ 24,50\n\nTOTAL R$ 49,90\nFORMA DE PAGTO: DEBITO`;

  showCreateTransactionModal = false;
  selectedScan: ReceiptScan | null = null;

  transForm = {
    description: '',
    amount: 0,
    type: 'EXPENSE' as 'INCOME' | 'EXPENSE',
    date: new Date().toISOString().split('T')[0],
    accountId: '',
    categoryId: ''
  };

  constructor(
    private receiptService: ReceiptService,
    private transactionService: TransactionService,
    private accountService: AccountService,
    private categoryService: CategoryService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadScans();
    this.loadAccounts();
    this.loadCategories();
  }

  loadScans(): void {
    this.loading = true;
    this.errorMessage = '';
    this.receiptService.listScans().subscribe({
      next: (data) => {
        this.scans = data;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = 'Erro ao carregar recibos: ' + (err.error?.message || err.message);
        this.loading = false;
      }
    });
  }

  loadAccounts(): void {
    this.accountService.listAccounts().subscribe({
      next: (accs) => (this.accounts = accs)
    });
  }

  loadCategories(): void {
    this.categoryService.listCategories().subscribe({
      next: (cats) => (this.categories = cats)
    });
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (!file) return;

    this.scanning = true;
    this.receiptService.scanReceipt(file).subscribe({
      next: (scan) => {
        this.scanning = false;
        this.successMessage = `Cupom escaneado com sucesso! Estabelecimento: ${scan.extractedMerchant || 'Identificado'}, Valor: R$ ${scan.extractedAmount || 0}`;
        this.loadScans();
        this.openCreateTransFromScan(scan);
        setTimeout(() => (this.successMessage = ''), 5000);
      },
      error: (err) => {
        this.scanning = false;
        this.errorMessage = 'Erro ao escanear cupom: ' + (err.error?.message || err.message);
      }
    });
  }

  runSimulatedScan(): void {
    this.scanning = true;
    this.receiptService.simulateScan({ receiptText: this.simulatedText }).subscribe({
      next: (scan) => {
        this.scanning = false;
        this.successMessage = 'Leitura OCR simulada com sucesso!';
        this.loadScans();
        this.openCreateTransFromScan(scan);
        setTimeout(() => (this.successMessage = ''), 5000);
      },
      error: (err) => {
        this.scanning = false;
        this.errorMessage = 'Erro ao simular scan: ' + (err.error?.message || err.message);
      }
    });
  }

  openCreateTransFromScan(scan: ReceiptScan): void {
    this.selectedScan = scan;
    this.transForm = {
      description: scan.extractedMerchant ? `Compra em ${scan.extractedMerchant}` : 'Cupom Fiscal',
      amount: scan.extractedAmount || 49.90,
      type: 'EXPENSE',
      date: scan.extractedDate ? scan.extractedDate.split('T')[0] : new Date().toISOString().split('T')[0],
      accountId: this.accounts.length > 0 ? this.accounts[0].id : '',
      categoryId: scan.suggestedCategoryId || (this.categories.length > 0 ? this.categories[0].id : '')
    };
    this.showCreateTransactionModal = true;
  }

  saveTransaction(): void {
    if (!this.transForm.description || this.transForm.amount <= 0 || !this.transForm.accountId) {
      this.errorMessage = 'Preencha todos os campos obrigatórios da transação.';
      return;
    }

    const currentUserId = this.authService.currentUser?.id || '';
    const payload = {
      userId: currentUserId,
      accountId: this.transForm.accountId,
      categoryId: this.transForm.categoryId || null,
      type: this.transForm.type,
      description: this.transForm.description,
      amount: this.transForm.amount,
      transactionDate: this.transForm.date
    };

    this.transactionService.createTransaction(payload).subscribe({
      next: () => {
        this.successMessage = 'Transação importada e salva com sucesso no extrato!';
        this.showCreateTransactionModal = false;
        setTimeout(() => (this.successMessage = ''), 4000);
      },
      error: (err) => {
        this.errorMessage = 'Erro ao salvar transação: ' + (err.error?.message || err.message);
      }
    });
  }
}
