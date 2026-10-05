import { environment } from '../../../environments/environment';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss', './auth-brand.scss']
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
  capsLockOn = false;
  isDark = true;

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
    this.initTheme();
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

  /** Mesma chave e mesma regra do shell: o tema escolhido aqui continua valendo depois do login. */
  private initTheme(): void {
    this.isDark = localStorage.getItem('controlei-theme') !== 'light';
    this.applyTheme();
  }

  toggleTheme(): void {
    this.isDark = !this.isDark;
    localStorage.setItem('controlei-theme', this.isDark ? 'dark' : 'light');
    this.applyTheme();
  }

  private applyTheme(): void {
    if (this.isDark) {
      document.documentElement.removeAttribute('data-theme');
    } else {
      document.documentElement.setAttribute('data-theme', 'light');
    }
  }

  checkCapsLock(event: Event): void {
    if (event instanceof KeyboardEvent && typeof event.getModifierState === 'function') {
      this.capsLockOn = event.getModifierState('CapsLock');
    }
  }

  isInvalid(form: FormGroup, control: string): boolean {
    const c = form.get(control);
    return !!c && c.invalid && c.touched;
  }

  get confirmMismatch(): boolean {
    const c = this.registerForm.get('confirmPassword');
    return !!c && c.touched && c.hasError('mismatch');
  }

  get confirmMatches(): boolean {
    const { password, confirmPassword } = this.registerForm.value;
    return !!password && password === confirmPassword && !this.registerForm.get('password')?.invalid;
  }

  /**
   * Indicador visual apenas: a regra que vale e a do formulario (10 a 72 caracteres). Ele so orienta a escolher uma
   * senha melhor que o minimo.
   */
  get passwordStrength(): { level: number; label: string } {
    const value: string = this.registerForm.get('password')?.value ?? '';
    if (!value) {
      return { level: 0, label: 'Use 10 caracteres ou mais, misturando letras, números e símbolos.' };
    }
    if (value.length < 10) {
      return { level: 1, label: `Faltam ${10 - value.length} caractere${10 - value.length === 1 ? '' : 's'}` };
    }
    let variety = 0;
    if (/[a-z]/.test(value)) variety++;
    if (/[A-Z]/.test(value)) variety++;
    if (/\d/.test(value)) variety++;
    if (/[^A-Za-z0-9]/.test(value)) variety++;
    if (value.length >= 14 && variety >= 3) {
      return { level: 4, label: 'Senha forte' };
    }
    if (variety >= 3 || (value.length >= 14 && variety >= 2)) {
      return { level: 3, label: 'Senha boa' };
    }
    return { level: 2, label: 'Senha razoável: misture maiúsculas, números ou símbolos' };
  }

  private passwordsMatch(group: FormGroup): { [key: string]: boolean } | null {
    const password = group.get('password')?.value;
    const confirmControl = group.get('confirmPassword');
    const confirm = confirmControl?.value;
    if (password && confirm && password !== confirm) {
      confirmControl?.setErrors({ ...confirmControl.errors, mismatch: true });
      return { mismatch: true };
    }
    // Corrigir a SENHA (e nao a confirmacao) tambem precisa apagar o "nao conferem", senao o formulario fica preso.
    if (confirmControl?.hasError('mismatch')) {
      const { mismatch, ...rest } = confirmControl.errors ?? {};
      confirmControl.setErrors(Object.keys(rest).length ? rest : null);
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
