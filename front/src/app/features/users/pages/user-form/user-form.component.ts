import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService, CreateUserRequest, UpdateUserRequest } from '../../../../core/services/user.service';

@Component({
  selector: 'app-user-form',
  standalone: false,
  templateUrl: './user-form.component.html',
  styleUrl: './user-form.component.scss'
})
export class UserFormComponent implements OnInit {
  form!: FormGroup;
  loading = false;
  saving = false;
  errorMessage = '';
  isEdit = false;
  userId: string | null = null;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.userId = this.route.snapshot.paramMap.get('id');
    this.isEdit = !!this.userId;

    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(255)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', this.isEdit ? [] : [Validators.required, Validators.minLength(6)]],
      role: ['MEMBER', [Validators.required]]
    });

    if (this.isEdit) {
      this.loadUser();
    }
  }

  private loadUser(): void {
    this.loading = true;
    this.userService.getUser(this.userId!).subscribe({
      next: (user) => {
        this.form.patchValue({
          name: user.name,
          email: user.email,
          role: user.role
        });
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Erro ao carregar usuário.';
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

    if (this.isEdit) {
      const request: UpdateUserRequest = {
        name: this.form.value.name,
        email: this.form.value.email
      };
      if (this.form.value.password) {
        request.password = this.form.value.password;
      }
      this.userService.updateUser(this.userId!, request).subscribe({
        next: () => this.router.navigate(['/app/users']),
        error: (err) => {
          this.saving = false;
          this.errorMessage = err?.message || 'Erro ao atualizar usuário.';
        }
      });
    } else {
      const request: CreateUserRequest = {
        name: this.form.value.name,
        email: this.form.value.email,
        password: this.form.value.password,
        role: this.form.value.role
      };
      this.userService.createUser(request).subscribe({
        next: () => this.router.navigate(['/app/users']),
        error: (err) => {
          this.saving = false;
          this.errorMessage = err?.message || 'Erro ao criar usuário.';
        }
      });
    }
  }
}
