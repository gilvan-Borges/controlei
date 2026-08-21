import { Component, OnInit } from '@angular/core';
import { BudgetService } from '../../../core/services/budget.service';
import { CategoryService } from '../../categories/services/category.service';
import { Budget, BudgetSummary, CreateBudgetRequest } from '../../../core/models/budget.model';
import { Category } from '../../../core/models/category.model';

@Component({
  selector: 'app-budgets-page',
  standalone: false,
  templateUrl: './budgets-page.component.html',
  styleUrl: './budgets-page.component.scss'
})
export class BudgetsPageComponent implements OnInit {
  year = new Date().getFullYear();
  month = new Date().getMonth() + 1;
  loading = false;
  errorMessage = '';
  successMessage = '';

  budgets: Budget[] = [];
  summary: BudgetSummary | null = null;
  categories: Category[] = [];

  showModal = false;
  newBudget: CreateBudgetRequest = {
    categoryId: '',
    year: this.year,
    month: this.month,
    plannedAmount: 500,
    alertThresholdPercent: 80
  };

  months = [
    { value: 1, label: 'Janeiro' }, { value: 2, label: 'Fevereiro' },
    { value: 3, label: 'Março' }, { value: 4, label: 'Abril' },
    { value: 5, label: 'Maio' }, { value: 6, label: 'Junho' },
    { value: 7, label: 'Julho' }, { value: 8, label: 'Agosto' },
    { value: 9, label: 'Setembro' }, { value: 10, label: 'Outubro' },
    { value: 11, label: 'Novembro' }, { value: 12, label: 'Dezembro' }
  ];

  constructor(
    private budgetService: BudgetService,
    private categoryService: CategoryService
  ) {}

  ngOnInit(): void {
    this.loadData();
    this.loadCategories();
  }

  loadData(): void {
    this.loading = true;
    this.errorMessage = '';
    this.budgetService.list(this.year, this.month).subscribe({
      next: (data) => {
        this.budgets = data;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = 'Erro ao carregar orçamentos: ' + (err.error?.message || err.message);
        this.loading = false;
      }
    });

    this.budgetService.getSummary(this.year, this.month).subscribe({
      next: (sum) => (this.summary = sum),
      error: () => {}
    });
  }

  loadCategories(): void {
    this.categoryService.listCategories().subscribe({
      next: (cats) => {
        this.categories = cats;
        if (cats.length > 0 && !this.newBudget.categoryId) {
          this.newBudget.categoryId = cats[0].id;
        }
      }
    });
  }

  onPeriodSelect(period: { month: number; year: number }): void {
    this.month = period.month;
    this.year = period.year;
    this.loadData();
  }

  onPeriodChange(): void {
    this.loadData();
  }

  openCreateModal(): void {
    this.newBudget = {
      categoryId: this.categories.length > 0 ? this.categories[0].id : '',
      year: this.year,
      month: this.month,
      plannedAmount: 500,
      alertThresholdPercent: 80
    };
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  saveBudget(): void {
    if (!this.newBudget.categoryId || this.newBudget.plannedAmount <= 0) {
      this.errorMessage = 'Preencha a categoria e um limite válido.';
      return;
    }

    this.budgetService.create(this.newBudget).subscribe({
      next: () => {
        this.successMessage = 'Orçamento cadastrado com sucesso!';
        this.showModal = false;
        this.loadData();
        setTimeout(() => (this.successMessage = ''), 4000);
      },
      error: (err) => {
        this.errorMessage = 'Erro ao salvar orçamento: ' + (err.error?.message || err.message);
      }
    });
  }

  deleteBudget(id: string): void {
    if (confirm('Deseja excluir este orçamento?')) {
      this.budgetService.delete(id).subscribe({
        next: () => {
          this.loadData();
        }
      });
    }
  }

  getProgressColor(percentage: number): string {
    if (percentage > 100) return 'danger';
    if (percentage >= 80) return 'warning';
    return 'primary';
  }
}
