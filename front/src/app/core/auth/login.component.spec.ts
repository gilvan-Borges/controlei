import { ChangeDetectorRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { vi } from 'vitest';
import { LoginComponent } from './login.component';
import { environment } from '../../../environments/environment';

/**
 * O cadastro chamava POST /users (criar MEMBRO, exige login) em vez de POST /auth/register-family: ninguem conseguia se
 * cadastrar, e o 401 virava "Sessao expirada". Estes testes travam o contrato certo.
 */
describe('LoginComponent: cadastro de familia', () => {
  let http: HttpTestingController;
  let router: Router;

  const fill = (component: LoginComponent) =>
    component.registerForm.setValue({
      familyName: 'Familia Silva',
      name: 'Joao da Silva',
      email: 'joao@example.com',
      password: 'uma-senha-longa-123',
      confirmPassword: 'uma-senha-longa-123'
    });

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, RouterModule.forRoot([]), HttpClientTestingModule],
      declarations: [LoginComponent]
    });
    http = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
  });

  afterEach(() => localStorage.clear());

  it('cria a familia pela rota publica e entra logado no painel', () => {
    const component = TestBed.createComponent(LoginComponent).componentInstance;
    fill(component);

    component.onRegisterSubmit();

    const req = http.expectOne(`${environment.apiUrl}/auth/register-family`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      familyName: 'Familia Silva',
      responsibleName: 'Joao da Silva',
      email: 'joao@example.com',
      password: 'uma-senha-longa-123'
    });
    req.flush({
      accessToken: 'a',
      refreshToken: 'r',
      tokenType: 'Bearer',
      expiresIn: 900,
      user: { id: '1', name: 'Joao da Silva', email: 'joao@example.com', familyId: 'f', role: 'RESPONSIBLE', active: true }
    });

    expect(localStorage.getItem('controlei_token')).toBe('a');
    expect(router.navigate).toHaveBeenCalledWith(['/app/dashboard']);
  });

  it('nunca chama a rota de criar membro (que exige login)', () => {
    const component = TestBed.createComponent(LoginComponent).componentInstance;
    fill(component);

    component.onRegisterSubmit();

    http.expectNone(`${environment.apiUrl}/users`);
    http.expectOne(`${environment.apiUrl}/auth/register-family`);
  });

  it('mostra a mensagem do servidor quando o cadastro falha, sem mandar para o login', () => {
    const component = TestBed.createComponent(LoginComponent).componentInstance;
    fill(component);

    component.onRegisterSubmit();
    http
      .expectOne(`${environment.apiUrl}/auth/register-family`)
      .flush({ message: 'Email ja cadastrado' }, { status: 422, statusText: 'Unprocessable' });

    expect(component.registerError).toBeTruthy();
    expect(component.registerLoading).toBe(false);
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('exige o nome da familia e senha de pelo menos 10 caracteres', () => {
    const component = TestBed.createComponent(LoginComponent).componentInstance;
    component.registerForm.setValue({
      familyName: '',
      name: 'Joao',
      email: 'joao@example.com',
      password: 'curta',
      confirmPassword: 'curta'
    });

    component.onRegisterSubmit();

    http.expectNone(`${environment.apiUrl}/auth/register-family`);
    expect(component.registerForm.get('familyName')?.invalid).toBe(true);
    expect(component.registerForm.get('password')?.invalid).toBe(true);
  });
});

describe('LoginComponent: experiencia da tela', () => {
  beforeEach(() => {
    localStorage.clear();
    document.documentElement.removeAttribute('data-theme');
    TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, RouterModule.forRoot([]), HttpClientTestingModule],
      declarations: [LoginComponent]
    });
  });

  afterEach(() => {
    localStorage.clear();
    document.documentElement.removeAttribute('data-theme');
  });

  it('corrigir a senha (e nao a confirmacao) libera o formulario', () => {
    const component = TestBed.createComponent(LoginComponent).componentInstance;
    component.registerForm.setValue({
      familyName: 'Familia Silva',
      name: 'Joao',
      email: 'joao@example.com',
      password: 'uma-senha-longa-12',
      confirmPassword: 'uma-senha-longa-123'
    });
    expect(component.registerForm.get('confirmPassword')?.hasError('mismatch')).toBe(true);

    component.registerForm.get('password')?.setValue('uma-senha-longa-123');

    expect(component.registerForm.get('confirmPassword')?.hasError('mismatch')).toBe(false);
    expect(component.registerForm.valid).toBe(true);
  });

  it('mede a forca da senha sem mudar a regra do formulario', () => {
    const component = TestBed.createComponent(LoginComponent).componentInstance;
    const strengthOf = (value: string) => {
      component.registerForm.get('password')?.setValue(value);
      return component.passwordStrength.level;
    };

    expect(strengthOf('')).toBe(0);
    expect(strengthOf('curta')).toBe(1);
    expect(strengthOf('somenteletras')).toBe(2);
    expect(strengthOf('Senha12345')).toBe(3);
    expect(strengthOf('Uma-Senha-Forte-2024')).toBe(4);
  });

  it('alterna o tema e guarda a escolha com a mesma chave do app', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    expect(component.isDark).toBe(true);

    component.toggleTheme();

    expect(document.documentElement.getAttribute('data-theme')).toBe('light');
    expect(localStorage.getItem('controlei-theme')).toBe('light');
  });

  /**
   * Responde o GET /auth/config e redesenha. Os testes rodam sem zone.js (o app usa), entao a resposta simulada nao
   * marca o componente sozinha: marcamos aqui, como a zona faria.
   */
  const answerConfig = (
    fixture: ComponentFixture<LoginComponent>,
    config: { registrationEnabled: boolean; demoEnabled: boolean }
  ) => {
    TestBed.inject(HttpTestingController).expectOne(`${environment.apiUrl}/auth/config`).flush(config);
    fixture.componentRef.injector.get(ChangeDetectorRef).markForCheck();
    fixture.detectChanges();
  };

  it('troca entre entrar e criar conta pelas abas', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
    answerConfig(fixture, { registrationEnabled: true, demoEnabled: false });
    const el: HTMLElement = fixture.nativeElement;

    (el.querySelector('#tab-register') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(el.querySelector('#regFamily')).toBeTruthy();
    expect(el.querySelector('#loginEmail')).toBeNull();
    expect(el.querySelector('#tab-register')?.getAttribute('aria-selected')).toBe('true');
  });

  it('com o cadastro fechado, esconde a aba e os convites para criar conta', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
    answerConfig(fixture, { registrationEnabled: false, demoEnabled: false });
    const el: HTMLElement = fixture.nativeElement;

    expect(el.querySelector('#tab-register')).toBeNull();
    expect(el.textContent).not.toContain('Crie a da sua família');
    fixture.componentInstance.goToRegister();
    expect(fixture.componentInstance.isRegister).toBe(false);
  });

  it('no modo demonstracao, entra como visitante sem senha e vai ao painel', () => {
    const router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
    answerConfig(fixture, { registrationEnabled: false, demoEnabled: true });
    const el: HTMLElement = fixture.nativeElement;

    (el.querySelector('.btn-visitor') as HTMLButtonElement).click();
    const req = TestBed.inject(HttpTestingController).expectOne(`${environment.apiUrl}/auth/demo`);
    expect(req.request.method).toBe('POST');
    req.flush({
      accessToken: 'demo-token',
      refreshToken: 'r',
      tokenType: 'Bearer',
      expiresIn: 900,
      user: { id: '1', name: 'Visitante', email: 'visitante@demo.controlei', familyId: 'f', role: 'RESPONSIBLE', active: true }
    });

    expect(localStorage.getItem('controlei_token')).toBe('demo-token');
    expect(router.navigate).toHaveBeenCalledWith(['/app/dashboard']);
  });
});
