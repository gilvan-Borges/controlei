import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DebtService } from '../../services/debt.service';
import { UserService } from '../../../../core/services/user.service';
import { CategoryService } from '../../../categories/services/category.service';
import { User } from '../../../../core/models/user.model';
import { Category } from '../../../../core/models/category.model';

@Component({
  selector: 'app-debt-form',
  standalone: false,
  templateUrl: './debt-form.component.html',
  styleUrl: './debt-form.component.scss'
})
export class DebtFormComponent implements OnInit {
  form!: FormGroup;
  loading = false;
  saving = false;
  errorMessage = '';
  isEdit = false;
  debtId: string | null = null;

  users: User[] = [];
  categories: Category[] = [];

  constructor(
    private fb: FormBuilder,
    private debtService: DebtService,
    private userService: UserService,
    private categoryService: CategoryService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.debtId = this.route.snapshot.paramMap.get('id');
    this.isEdit = !!this.debtId;

    this.form = this.fb.group({
      userId: ['', [Validators.required]],
      categoryId: [null],
      description: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(500)]],
      purchaseDate: ['', [Validators.required]],
      totalAmount: [0, [Validators.required, Validators.min(0.01)]],
      installmentCount: [1, [Validators.required, Validators.min(1)]],
      firstDueDate: ['', this.isEdit ? [] : [Validators.required]],
      notes: ['', [Validators.maxLength(2000)]]
    });

    this.loadDropdowns();

    if (this.isEdit) {
      this.loadDebt();
    }
  }

  private loadDropdowns(): void {
    this.userService.listUsers().subscribe({ next: (u) => this.users = u, error: () => {} });
    this.categoryService.listCategories({ type: 'DEBT' }).subscribe({ next: (c) => this.categories = c, error: () => {} });
  }

  private loadDebt(): void {
    this.loading = true;
    this.debtService.getDebt(this.debtId!).subscribe({
      next: (debt) => {
        this.form.patchValue({
          userId: debt.userId,
          categoryId: debt.categoryId,
          description: debt.description,
          purchaseDate: debt.purchaseDate,
          totalAmount: debt.totalAmount,
          installmentCount: debt.installmentCount,
          notes: debt.notes
        });
        this.form.get('totalAmount')!.disable();
        this.form.get('installmentCount')!.disable();
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Erro ao carregar dívida.';
        this.loading = false;
      }
    });
  }

  get estimatedInstallmentAmount(): number {
    const total = this.form.get('totalAmount')?.value || 0;
    const count = this.form.get('installmentCount')?.value || 1;
    return count > 0 ? total / count : 0;
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.errorMessage = '';

    const formValue = this.form.getRawValue();
    const request: any = {
      userId: formValue.userId,
      description: formValue.description,
      purchaseDate: formValue.purchaseDate,
      totalAmount: formValue.totalAmount,
      installmentCount: formValue.installmentCount,
      firstDueDate: formValue.firstDueDate
    };
    if (formValue.categoryId) request.categoryId = formValue.categoryId;
    if (formValue.notes) request.notes = formValue.notes;

    if (this.isEdit) {
      const updateRequest: any = {
        description: formValue.description,
        purchaseDate: formValue.purchaseDate
      };
      if (formValue.categoryId) updateRequest.categoryId = formValue.categoryId;
      if (formValue.notes) updateRequest.notes = formValue.notes;

      this.debtService.updateDebt(this.debtId!, updateRequest).subscribe({
        next: () => this.router.navigate(['/app/debts']),
        error: (err) => {
          this.saving = false;
          this.errorMessage = err?.message || 'Erro ao atualizar dívida.';
        }
      });
    } else {
      this.debtService.createDebt(request).subscribe({
        next: () => this.router.navigate(['/app/debts']),
        error: (err) => {
          this.saving = false;
          this.errorMessage = err?.message || 'Erro ao criar dívida.';
        }
      });
    }
  }
}
