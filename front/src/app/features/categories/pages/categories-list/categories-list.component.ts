import { Component, OnInit } from '@angular/core';
import { CategoryService } from '../../services/category.service';
import { Category, CategoryType } from '../../../../core/models/category.model';

@Component({
  selector: 'app-categories-list',
  standalone: false,
  templateUrl: './categories-list.component.html',
  styleUrl: './categories-list.component.scss'
})
export class CategoriesListComponent implements OnInit {
  categories: Category[] = [];
  loading = false;
  errorMessage = '';
  showDeleteConfirm = false;
  categoryToDelete: Category | null = null;

  filterType: CategoryType | '' = '';

  categoryTypes: { value: CategoryType; label: string }[] = [
    { value: 'INCOME', label: 'Receita' },
    { value: 'EXPENSE', label: 'Despesa' },
    { value: 'DEBT', label: 'Dívida' },
    { value: 'INVESTMENT', label: 'Investimento' }
  ];

  constructor(private categoryService: CategoryService) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.loading = true;
    this.errorMessage = '';
    const filters: any = {};
    if (this.filterType) filters.type = this.filterType;

    this.categoryService.listCategories(filters).subscribe({
      next: (categories) => {
        this.categories = categories;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Erro ao carregar categorias.';
        this.loading = false;
      }
    });
  }

  getTypeLabel(type: CategoryType): string {
    return this.categoryTypes.find(t => t.value === type)?.label || type;
  }

  confirmDelete(category: Category): void {
    this.categoryToDelete = category;
    this.showDeleteConfirm = true;
  }

  onDeleteConfirm(): void {
    if (!this.categoryToDelete) return;
    this.categoryService.deleteCategory(this.categoryToDelete.id).subscribe({
      next: () => {
        this.showDeleteConfirm = false;
        this.categoryToDelete = null;
        this.loadCategories();
      },
      error: (err) => {
        this.showDeleteConfirm = false;
        this.errorMessage = err?.message || 'Erro ao excluir categoria.';
      }
    });
  }

  onDeleteCancel(): void {
    this.showDeleteConfirm = false;
    this.categoryToDelete = null;
  }

  onFilterChange(): void {
    this.loadCategories();
  }
}
