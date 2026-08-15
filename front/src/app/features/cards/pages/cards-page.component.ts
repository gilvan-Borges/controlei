import { Component, OnInit } from '@angular/core';
import { CreditCardService } from '../../../core/services/credit-card.service';
import { AccountService } from '../../accounts/services/account.service';
import { CategoryService } from '../../categories/services/category.service';
import { CreditCard, CreateCreditCardRequest, CreateCardExpenseRequest, Invoice, PayInvoiceRequest } from '../../../core/models/credit-card.model';
import { Account } from '../../../core/models/account.model';
import { Category } from '../../../core/models/category.model';

@Component({
  selector: 'app-cards-page',
  standalone: false,
  templateUrl: './cards-page.component.html',
  styleUrl: './cards-page.component.scss'
})
export class CardsPageComponent implements OnInit {
  cards: CreditCard[] = [];
  accounts: Account[] = [];
  categories: Category[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';

  selectedCard: CreditCard | null = null;
  invoices: Invoice[] = [];
  selectedInvoice: Invoice | null = null;

  // Modals
  showCreateCardModal = false;
  showExpenseModal = false;
  showPayInvoiceModal = false;

  newCard: CreateCreditCardRequest = {
    name: 'Nubank Ultravioleta',
    creditLimit: 5000,
    closingDay: 25,
    dueDay: 5,
    brand: 'Mastercard',
    lastDigits: '8842'
  };

  cardExpense: CreateCardExpenseRequest = {
    description: '',
    amount: 100,
    categoryId: '',
    transactionDate: new Date().toISOString().split('T')[0],
    totalInstallments: 1
  };

  payInvoiceReq: PayInvoiceRequest = {
    paymentDate: new Date().toISOString().split('T')[0],
    amount: 0,
    accountId: ''
  };

  constructor(
    private cardService: CreditCardService,
    private accountService: AccountService,
    private categoryService: CategoryService
  ) {}

  ngOnInit(): void {
    this.loadCards();
    this.loadAccounts();
    this.loadCategories();
  }

  loadCards(): void {
    this.loading = true;
    this.errorMessage = '';
    this.cardService.listCards().subscribe({
      next: (data) => {
        this.cards = data;
        this.loading = false;
        if (data.length > 0 && !this.selectedCard) {
          this.selectCard(data[0]);
        }
      },
      error: (err) => {
        this.errorMessage = 'Erro ao carregar cartões: ' + (err.error?.message || err.message);
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
      next: (cats) => {
        this.categories = cats;
        if (cats.length > 0) {
          this.cardExpense.categoryId = cats[0].id;
        }
      }
    });
  }

  selectCard(card: CreditCard): void {
    this.selectedCard = card;
    this.cardService.listInvoices(card.id).subscribe({
      next: (invs) => (this.invoices = invs),
      error: () => (this.invoices = [])
    });
  }

  openCreateCardModal(): void {
    this.newCard = {
      name: '',
      creditLimit: 5000,
      closingDay: 25,
      dueDay: 5,
      brand: 'Mastercard',
      lastDigits: '1234'
    };
    this.showCreateCardModal = true;
  }

  openExpenseModal(card: CreditCard): void {
    this.selectedCard = card;
    this.cardExpense = {
      description: '',
      amount: 100,
      categoryId: this.categories.length > 0 ? this.categories[0].id : '',
      transactionDate: new Date().toISOString().split('T')[0],
      totalInstallments: 1
    };
    this.showExpenseModal = true;
  }

  openPayModal(invoice: Invoice): void {
    this.selectedInvoice = invoice;
    this.payInvoiceReq = {
      paymentDate: new Date().toISOString().split('T')[0],
      amount: invoice.totalAmount,
      accountId: this.accounts.length > 0 ? this.accounts[0].id : ''
    };
    this.showPayInvoiceModal = true;
  }

  saveCard(): void {
    if (!this.newCard.name || this.newCard.creditLimit <= 0) {
      this.errorMessage = 'Preencha o nome do cartão e limite válido.';
      return;
    }

    this.cardService.createCard(this.newCard).subscribe({
      next: (saved) => {
        this.successMessage = 'Cartão de crédito cadastrado!';
        this.showCreateCardModal = false;
        this.loadCards();
        this.selectCard(saved);
        setTimeout(() => (this.successMessage = ''), 4000);
      },
      error: (err) => {
        this.errorMessage = 'Erro ao salvar cartão: ' + (err.error?.message || err.message);
      }
    });
  }

  submitExpense(): void {
    if (!this.selectedCard || !this.cardExpense.description || this.cardExpense.amount <= 0) return;

    this.cardService.addExpense(this.selectedCard.id, this.cardExpense).subscribe({
      next: () => {
        this.successMessage = 'Compra no cartão lançada com sucesso!';
        this.showExpenseModal = false;
        this.loadCards();
        if (this.selectedCard) this.selectCard(this.selectedCard);
        setTimeout(() => (this.successMessage = ''), 4000);
      },
      error: (err) => {
        this.errorMessage = 'Erro ao lançar despesa no cartão: ' + (err.error?.message || err.message);
      }
    });
  }

  submitPayment(): void {
    if (!this.selectedInvoice || !this.payInvoiceReq.amount || this.payInvoiceReq.amount <= 0) return;

    this.cardService.payInvoice(this.selectedInvoice.id, this.payInvoiceReq).subscribe({
      next: () => {
        this.successMessage = 'Fatura paga e quitada com sucesso!';
        this.showPayInvoiceModal = false;
        this.loadCards();
        if (this.selectedCard) this.selectCard(this.selectedCard);
        setTimeout(() => (this.successMessage = ''), 4000);
      },
      error: (err) => {
        this.errorMessage = 'Erro ao pagar fatura: ' + (err.error?.message || err.message);
      }
    });
  }

  deleteCard(id: string): void {
    if (confirm('Deseja excluir este cartão de crédito?')) {
      this.cardService.deleteCard(id).subscribe({
        next: () => {
          this.selectedCard = null;
          this.loadCards();
        }
      });
    }
  }

  getTotalCreditLimit(): number {
    return this.cards.reduce((acc, c) => acc + (c.creditLimit || 0), 0);
  }

  getTotalAvailableLimit(): number {
    return this.cards.reduce((acc, c) => acc + (c.availableLimit || 0), 0);
  }
}
