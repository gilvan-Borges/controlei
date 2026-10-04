import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TransactionService } from '../../services/transaction.service';
import { UserService } from '../../../../core/services/user.service';
import { CategoryService } from '../../../categories/services/category.service';
import { AccountService } from '../../../accounts/services/account.service';
import { TransactionType } from '../../../../core/models/transaction.model';
import { User } from '../../../../core/models/user.model';
import { Category } from '../../../../core/models/category.model';
import { Account } from '../../../../core/models/account.model';

@Component({
  selector: 'app-transaction-form',
  standalone: false,
  templateUrl: './transaction-form.component.html',
  styleUrl: './transaction-form.component.scss'
})
export class TransactionFormComponent implements OnInit {
  form!: FormGroup;
  loading = false;
  saving = false;
  errorMessage = '';
  isEdit = false;
  transactionId: string | null = null;
  transactionType: TransactionType = 'EXPENSE';

  users: User[] = [];
  categories: Category[] = [];
  accounts: Account[] = [];

  constructor(
    private fb: FormBuilder,
    private transactionService: TransactionService,
    private userService: UserService,
    private categoryService: CategoryService,
    private accountService: AccountService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.transactionId = this.route.snapshot.paramMap.get('id');
    this.isEdit = !!this.transactionId;

    const typeFromRoute = this.route.snapshot.data?.['type'] as TransactionType;
    if (typeFromRoute) {
      this.transactionType = typeFromRoute;
    }

    this.form = this.fb.group({
      userId: ['', [Validators.required]],
      accountId: ['', [Validators.required]],
      categoryId: [null],
      type: [this.transactionType, [Validators.required]],
      description: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(500)]],
      amount: [0, [Validators.required, Validators.min(0.01)]],
      transactionDate: ['', [Validators.required]],
      dueDate: [null],
      notes: ['', [Validators.maxLength(2000)]]
    });

    this.loadDropdowns();

    if (this.isEdit) {
      this.loadTransaction();
    }
  }

  private loadDropdowns(): void {
    this.userService.listUsers().subscribe({ next: (u) => this.users = u, error: () => {} });
    this.categoryService.listCategories().subscribe({ next: (c) => this.categories = c, error: () => {} });
    this.accountService.listAccounts().subscribe({ next: (a) => this.accounts = a, error: () => {} });
  }

  private loadTransaction(): void {
    this.loading = true;
    this.transactionService.getTransaction(this.transactionId!).subscribe({
      next: (tx) => {
        this.transactionType = tx.type;
        this.form.patchValue({
          userId: tx.userId,
          accountId: tx.accountId,
          categoryId: tx.categoryId,
          type: tx.type,
          description: tx.description,
          amount: tx.amount,
          transactionDate: tx.transactionDate,
          dueDate: tx.dueDate,
          notes: tx.notes
        });
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Erro ao carregar transação.';
        this.loading = false;
      }
    });
  }

  get filteredCategories(): Category[] {
    const catType = this.transactionType === 'INCOME' ? 'INCOME' : 'EXPENSE';
    return this.categories.filter(c => c.type === catType || c.type === 'DEBT');
  }

  get pageTitle(): string {
    if (this.isEdit) return 'Editar Transação';
    return this.transactionType === 'INCOME' ? 'Nova Receita' : 'Nova Despesa';
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.errorMessage = '';

    const formValue = this.form.value;
    const request: any = {
      userId: formValue.userId,
      accountId: formValue.accountId,
      type: formValue.type,
      description: formValue.description,
      amount: formValue.amount,
      transactionDate: formValue.transactionDate
    };
    if (formValue.categoryId) request.categoryId = formValue.categoryId;
    if (formValue.dueDate) request.dueDate = formValue.dueDate;
    if (formValue.notes) request.notes = formValue.notes;

    if (this.isEdit) {
      this.transactionService.updateTransaction(this.transactionId!, request).subscribe({
        next: () => this.router.navigate(['/app/transactions']),
        error: (err) => {
          this.saving = false;
          this.errorMessage = err?.message || 'Erro ao atualizar transação.';
        }
      });
    } else {
      this.transactionService.createTransaction(request).subscribe({
        next: () => this.router.navigate(['/app/transactions']),
        error: (err) => {
          this.saving = false;
          this.errorMessage = err?.message || 'Erro ao criar transação.';
        }
      });
    }
  }
}
