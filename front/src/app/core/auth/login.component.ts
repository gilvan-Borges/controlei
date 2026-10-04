import { environment } from '../../../environments/environment';
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
  isRegister = false;
  loginForm: FormGroup;
  registerForm: FormGroup;

  loading = false;
  loginError = '';
  loginSuccess = '';

  registerLoading = false;
  registerError = '';
  registerSuccess = '';

  showPassword = false;
  showRegPassword = false;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });

    this.registerForm = this.fb.group({
      familyName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(255)]],
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(255)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(72)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordsMatch });
  }

  ngOnInit(): void {
    // Check initial route to set mode
    this.checkCurrentRoute();

    this.route.queryParams.subscribe(params => {
      if (params['registered'] === 'true') {
        this.loginSuccess = 'Conta criada com sucesso! Faça login para continuar.';
        this.isRegister = false;
      }
    });
  }

  private checkCurrentRoute(): void {
    const url = this.router.url;
    if (url.includes('/register')) {
      this.isRegister = true;
    } else {
      this.isRegister = false;
    }
  }

  goToRegister(event?: Event): void {
    if (event) event.preventDefault();
    this.isRegister = true;
    this.loginError = '';
    this.loginSuccess = '';
    window.history.pushState({}, '', '/register');
  }

  goToLogin(event?: Event): void {
    if (event) event.preventDefault();
    this.isRegister = false;
    this.registerError = '';
    this.registerSuccess = '';
    window.history.pushState({}, '', '/login');
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  toggleRegPasswordVisibility(): void {
    this.showRegPassword = !this.showRegPassword;
  }

  private passwordsMatch(group: FormGroup): { [key: string]: boolean } | null {
    const password = group.get('password')?.value;
    const confirm = group.get('confirmPassword')?.value;
    if (password && confirm && password !== confirm) {
      group.get('confirmPassword')?.setErrors({ mismatch: true });
      return { mismatch: true };
    }
    return null;
  }

  /** Contas de demonstracao: so existem no build de desenvolvimento (ver environment.ts). */
  readonly demo = environment.demoLogin;

  fillDemo(role: 'admin' | 'member'): void {
    const account = this.demo?.[role];
    if (account) {
      this.loginForm.patchValue({ email: account.email, password: account.password });
    }
  }

  onLoginSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.loginError = '';
    this.loginSuccess = '';

    const { email, password } = this.loginForm.value;

    this.authService.login(email, password).subscribe({
      next: () => {
        this.router.navigate(['/app/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        this.loginError = err?.message || 'Erro ao fazer login. Verifique seu e-mail e senha.';
      }
    });
  }

  onRegisterSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.registerLoading = true;
    this.registerError = '';
    this.registerSuccess = '';

    const { familyName, name, email, password } = this.registerForm.value;

    // Cria a familia e o responsavel e ja devolve a sessao: nao ha motivo para mandar a pessoa digitar tudo de novo.
    // (Antes esta tela chamava POST /users, que cria um MEMBRO de uma familia existente e exige estar logado: por isso
    // ninguem conseguia se cadastrar e o erro 401 virava "Sessao expirada".)
    this.authService.registerFamily({
      familyName: familyName.trim(),
      responsibleName: name.trim(),
      email: email.trim(),
      password
    }).subscribe({
      next: () => {
        this.registerLoading = false;
        this.router.navigate(['/app/dashboard']);
      },
      error: (err) => {
        this.registerLoading = false;
        this.registerError = err?.message || 'Erro ao criar conta. Tente novamente.';
      }
    });
  }
}
