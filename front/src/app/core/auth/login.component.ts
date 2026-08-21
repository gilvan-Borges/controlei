import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  loading = false;
  errorMessage = '';
  successMessage = '';
  isDark = true;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService
  ) {
    this.initTheme();
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['registered'] === 'true') {
        this.successMessage = 'Conta criada com sucesso! Faça login para continuar.';
      }
    });
  }

  toggleTheme(): void {
    this.isDark = !this.isDark;
    const theme = this.isDark ? 'dark' : 'light';
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('controlei-theme', theme);
  }

  private initTheme(): void {
    const saved = localStorage.getItem('controlei-theme');
    if (saved === 'light') {
      this.isDark = false;
      document.documentElement.setAttribute('data-theme', 'light');
    } else {
      this.isDark = true;
      document.documentElement.removeAttribute('data-theme');
    }
  }

  fillDemo(role: 'admin' | 'member'): void {
    if (role === 'admin') {
      this.loginForm.patchValue({
        email: 'superadmin@controlei.local',
        password: 'Controlei@123'
      });
    } else {
      this.loginForm.patchValue({
        email: 'gilvan.borges@controlei.local',
        password: 'Controlei@123'
      });
    }
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const { email, password } = this.loginForm.value;

    this.authService.login(email, password).subscribe({
      next: () => {
        this.router.navigate(['/app/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err?.message || 'Erro ao fazer login. Verifique seu e-mail e senha.';
      }
    });
  }
}
