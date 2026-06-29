import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CategoryService } from '../../services/category.service';
import { CategoryType } from '../../../../core/models/category.model';

@Component({
  selector: 'app-category-form',
  standalone: false,
  templateUrl: './category-form.component.html',
  styleUrl: './category-form.component.scss'
})
export class CategoryFormComponent implements OnInit {
  form!: FormGroup;
  loading = false;
  saving = false;
  errorMessage = '';
  isEdit = false;
  categoryId: string | null = null;

  categoryTypes: { value: CategoryType; label: string }[] = [
    { value: 'INCOME', label: 'Receita' },
    { value: 'EXPENSE', label: 'Despesa' },
    { value: 'DEBT', label: 'Dívida' },
    { value: 'INVESTMENT', label: 'Investimento' }
  ];

  constructor(
    private fb: FormBuilder,
    private categoryService: CategoryService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.categoryId = this.route.snapshot.paramMap.get('id');
    this.isEdit = !!this.categoryId;

    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(255)]],
      type: ['EXPENSE', [Validators.required]],
      color: ['', [Validators.maxLength(50)]],
      icon: ['', [Validators.maxLength(255)]],
      active: [true]
    });

    if (this.isEdit) {
      this.loadCategory();
    }
  }

  private loadCategory(): void {
    this.loading = true;
    this.categoryService.getCategory(this.categoryId!).subscribe({
      next: (category) => {
        this.form.patchValue({
          name: category.name,
          type: category.type,
          color: category.color || '',
          icon: category.icon || '',
          active: category.active
        });
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Erro ao carregar categoria.';
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

    const formValue = this.form.value;
    const request: any = {
      name: formValue.name,
      type: formValue.type
    };
    if (formValue.color) request.color = formValue.color;
    if (formValue.icon) request.icon = formValue.icon;

    if (this.isEdit) {
      request.active = formValue.active;
      this.categoryService.updateCategory(this.categoryId!, request).subscribe({
        next: () => this.router.navigate(['/app/categories']),
        error: (err) => {
          this.saving = false;
          this.errorMessage = err?.message || 'Erro ao atualizar categoria.';
        }
      });
    } else {
      this.categoryService.createCategory(request).subscribe({
        next: () => this.router.navigate(['/app/categories']),
        error: (err) => {
          this.saving = false;
          this.errorMessage = err?.message || 'Erro ao criar categoria.';
        }
      });
    }
  }
}
