import { TestBed } from '@angular/core/testing';
import { HttpClient, HTTP_INTERCEPTORS } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Router, RouterModule } from '@angular/router';
import { vi } from 'vitest';
import { AuthInterceptor } from './auth.interceptor';
import { ErrorInterceptor } from './error.interceptor';
import { AuthService } from '../services/auth.service';
import { AlertService } from '../services/alert.service';
import { environment } from '../../../environments/environment';

describe('ErrorInterceptor: renovação transparente da sessão', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let router: Router;
  const alert = { toast: vi.fn() };

  const session = (access: string, refresh: string) => ({
    accessToken: access,
    refreshToken: refresh,
    tokenType: 'Bearer',
    expiresIn: 900,
    user: { id: '1', name: 'Ana', email: 'ana@email.com', familyId: 'f1', role: 'MEMBER', active: true }
  });

  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem('controlei_token', 'access-velho');
    localStorage.setItem('controlei_refresh_token', 'refresh-velho');
    alert.toast.mockClear();

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterModule.forRoot([])],
      providers: [
        AuthService,
        { provide: AlertService, useValue: alert },
        { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
        { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true }
      ]
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('renova o token e repete a requisição que recebeu 401', () => {
    let result: unknown;
    http.get('/api/dados').subscribe(r => (result = r));

    httpMock.expectOne('/api/dados').flush({}, { status: 401, statusText: 'Unauthorized' });

    const refresh = httpMock.expectOne(`${environment.apiUrl}/auth/refresh`);
    expect(refresh.request.body).toEqual({ refreshToken: 'refresh-velho' });
    refresh.flush(session('access-novo', 'refresh-novo'));

    const retry = httpMock.expectOne('/api/dados');
    expect(retry.request.headers.get('Authorization')).toBe('Bearer access-novo');
    retry.flush({ ok: true });

    expect(result).toEqual({ ok: true });
    expect(localStorage.getItem('controlei_refresh_token')).toBe('refresh-novo');
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('com várias requisições em 401 ao mesmo tempo faz UMA renovação só', () => {
    const results: unknown[] = [];
    http.get('/api/a').subscribe(r => results.push(r));
    http.get('/api/b').subscribe(r => results.push(r));

    httpMock.expectOne('/api/a').flush({}, { status: 401, statusText: 'Unauthorized' });
    httpMock.expectOne('/api/b').flush({}, { status: 401, statusText: 'Unauthorized' });

    // O refresh token é rotativo: duas renovações usariam o mesmo token e derrubariam a sessão
    const refresh = httpMock.expectOne(`${environment.apiUrl}/auth/refresh`);
    refresh.flush(session('access-novo', 'refresh-novo'));

    httpMock.expectOne('/api/a').flush({ a: 1 });
    httpMock.expectOne('/api/b').flush({ b: 2 });

    expect(results).toEqual([{ a: 1 }, { b: 2 }]);
  });

  it('se a renovação falha, encerra a sessão e manda para o login', () => {
    let error: { status: number } | undefined;
    http.get('/api/dados').subscribe({ error: e => (error = e) });

    httpMock.expectOne('/api/dados').flush({}, { status: 401, statusText: 'Unauthorized' });
    httpMock
      .expectOne(`${environment.apiUrl}/auth/refresh`)
      .flush({ message: 'expirado' }, { status: 401, statusText: 'Unauthorized' });
    // O logout avisa o servidor; não importa a resposta
    httpMock.match(`${environment.apiUrl}/auth/logout`).forEach(r => r.flush({}));

    expect(error?.status).toBe(401);
    expect(localStorage.getItem('controlei_token')).toBeNull();
    expect(localStorage.getItem('controlei_refresh_token')).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('um 401 no login não tenta renovar nem derruba sessão nenhuma', () => {
    let error: { status: number; message: string } | undefined;
    http.post(`${environment.apiUrl}/auth/login`, {}).subscribe({ error: e => (error = e) });

    httpMock
      .expectOne(`${environment.apiUrl}/auth/login`)
      .flush({ message: 'Credenciais invalidas' }, { status: 401, statusText: 'Unauthorized' });

    expect(error?.message).toBe('Credenciais invalidas');
    expect(router.navigate).not.toHaveBeenCalled();
    expect(localStorage.getItem('controlei_token')).toBe('access-velho');
    httpMock.verify();
  });

  it('sem refresh token, o 401 encerra a sessão direto', () => {
    localStorage.removeItem('controlei_refresh_token');
    let error: { status: number } | undefined;
    http.get('/api/dados').subscribe({ error: e => (error = e) });

    httpMock.expectOne('/api/dados').flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(error?.status).toBe(401);
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
    httpMock.expectNone(`${environment.apiUrl}/auth/refresh`);
  });

  it('403 de recurso fechado na demonstração mostra a mensagem do servidor e não sai da tela', () => {
    let erro: { status: number; message: string } | undefined;
    http.post(`${environment.apiUrl}/bank-connections`, {}).subscribe({ error: (e) => (erro = e) });
    httpMock.expectOne(`${environment.apiUrl}/bank-connections`).flush(
      { message: 'Indisponível na demonstração. Crie sua própria instância para usar este recurso.' },
      { status: 403, statusText: 'Forbidden' }
    );
    expect(erro?.message).toContain('Indisponível na demonstração');
    expect(router.navigate).not.toHaveBeenCalledWith(['/access-denied']);
    expect(alert.toast).toHaveBeenCalledWith(expect.stringContaining('demonstração'), 'info');
  });

  it('403 comum continua indo para a tela de acesso negado', () => {
    http.get(`${environment.apiUrl}/users`).subscribe({ error: () => undefined });
    httpMock.expectOne(`${environment.apiUrl}/users`).flush({ message: 'Acesso negado' }, { status: 403, statusText: 'Forbidden' });
    expect(router.navigate).toHaveBeenCalledWith(['/access-denied']);
  });
});
