import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, finalize, shareReplay, switchMap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { AlertService } from '../services/alert.service';
import { AuthResponse } from '../models/auth-response.model';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  /**
   * Renovação em andamento, compartilhada por todas as requisições que receberem 401 ao mesmo tempo.
   * O refresh token é rotativo: duas renovações paralelas usariam o mesmo token, o servidor leria o segundo uso como
   * reaproveitamento roubado e revogaria a sessão inteira. Por isso só existe UMA renovação por vez.
   */
  private refreshing$: Observable<AuthResponse> | null = null;

  constructor(
    private authService: AuthService,
    private router: Router,
    private alertService: AlertService
  ) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401 && this.canRefresh(req)) {
          return this.refreshOnce().pipe(
            // Repete a requisição original com o access token novo. Este interceptor fica "dentro" do
            // AuthInterceptor, então o cabeçalho é colocado aqui.
            switchMap(() => next.handle(this.withToken(req))),
            catchError(retryError => this.handleError(retryError, req))
          );
        }
        return this.handleError(error, req);
      })
    );
  }

  /** Só tenta renovar quando há refresh token e a chamada não é a própria autenticação. */
  private canRefresh(req: HttpRequest<unknown>): boolean {
    return !this.isAuthEndpoint(req.url) && !!this.authService.getRefreshToken();
  }

  private isAuthEndpoint(url: string): boolean {
    return url.includes('/auth/');
  }

  private withToken(req: HttpRequest<unknown>): HttpRequest<unknown> {
    const token = this.authService.getToken();
    return token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
  }

  private refreshOnce(): Observable<AuthResponse> {
    if (!this.refreshing$) {
      this.refreshing$ = this.authService.refreshToken().pipe(
        finalize(() => (this.refreshing$ = null)),
        shareReplay(1)
      );
    }
    return this.refreshing$;
  }

  private handleError(error: HttpErrorResponse, req: HttpRequest<unknown>): Observable<never> {
    let message = 'Erro inesperado. Tente novamente.';

    if (error.status === 0) {
      message = 'Não foi possível conectar ao servidor. Verifique sua conexão.';
      this.alertService.toast(message, 'error');
    } else if (error.status === 401) {
      if (this.isAuthEndpoint(req.url)) {
        // Login ou registro recusado: não é sessão expirada, e não há sessão para encerrar
        message = error.error?.message || 'Credenciais inválidas.';
      } else {
        this.authService.logout();
        this.router.navigate(['/login']);
        message = 'Sessão expirada. Faça login novamente.';
        this.alertService.toast(message, 'warning');
      }
    } else if (error.status === 403) {
      message = 'Você não tem permissão para acessar este recurso.';
      this.router.navigate(['/access-denied']);
      this.alertService.toast(message, 'error');
    } else if (error.status === 404) {
      message = error.error?.message || 'Recurso não encontrado.';
    } else if (error.status === 422 || error.status === 400) {
      message = error.error?.message || 'Dados inválidos. Verifique as informações preenchidas.';
    } else if (error.status >= 500) {
      message = 'Erro interno no servidor. Nossa equipe foi notificada.';
      this.alertService.toast(message, 'error');
    }

    return throwError(() => ({ status: error.status, message }));
  }
}
