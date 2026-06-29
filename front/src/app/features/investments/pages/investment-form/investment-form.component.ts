import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { InvestmentService } from '../../services/investment.service';
import { UserService } from '../../../../core/services/user.service';
import { CategoryService } from '../../../categories/services/category.service';
import { User } from '../../../../core/models/user.model';
import { Category } from '../../../../core/models/category.model';
import { InvestmentType } from '../../../../core/models/investment.model';

@Component({
  selector: 'app-investment-form',
  standalone: false,
  templateUrl: './investment-form.component.html',
  styleUrl: './investment-form.component.scss'
})
export class InvestmentFormComponent implements OnInit {
  form!: FormGroup;
  users: User[] = [];
  categories: Category[] = [];
  loading = false;
  saving = false;
  errorMessage = '';
  isEdit = false;
  investmentId: string | null = null;

  investmentTypes: { value: InvestmentType; label: string }[] = [
    { value: 'SAVINGS', label: 'Poupanca' },
    { value: 'FIXED_INCOME', label: 'Renda fixa' },
    { value: 'STOCK', label: 'Acao' },
    { value: 'FUND', label: 'Fundo' },
    { value: 'CRYPTO', label: 'Cripto' },
    { value: 'OTHER', label: 'Outro' }
  ];

  constructor(
    private fb: FormBuilder,
    private investmentService: InvestmentService,
    private userService: UserService,
    private categoryService: CategoryService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.investmentId = this.route.snapshot.paramMap.get('id');
    this.isEdit = !!this.investmentId;
    this.form = this.fb.group({
      userId: ['', [Validators.required]],
      categoryId: [null],
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(255)]],
      type: ['OTHER' satisfies InvestmentType, [Validators.required]],
      currentAmount: [0, [Validators.required, Validators.min(0)]],
      referenceDate: [''],
      notes: ['', [Validators.maxLength(2000)]]
    });

    this.userService.listUsers().subscribe({ next: users => this.users = users, error: () => {} });
    this.categoryService.listCategories({ type: 'INVESTMENT' }).subscribe({ next: categories => this.categories = categories, error: () => {} });

    if (this.isEdit) {
      this.loadInvestment();
    }
  }

  private loadInvestment(): void {
    this.loading = true;
    this.investmentService.getInvestment(this.investmentId!).subscribe({
      next: investment => {
        this.form.patchValue(investment);
        this.loading = false;
      },
      error: err => {
        this.errorMessage = err?.message || 'Erro ao carregar investimento.';
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    const value = this.form.value;
    const request = {
      ...value,
      categoryId: value.categoryId || null,
      referenceDate: value.referenceDate || null
    };

    const action$ = this.isEdit
      ? this.investmentService.updateInvestment(this.investmentId!, request)
      : this.investmentService.createInvestment(request);

    action$.subscribe({
      next: () => this.router.navigate(['/app/investments']),
      error: err => {
        this.errorMessage = err?.message || 'Erro ao salvar investimento.';
        this.saving = false;
      }
    });
  }
}
