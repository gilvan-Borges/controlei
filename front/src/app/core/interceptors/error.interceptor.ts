import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { AlertService } from '../services/alert.service';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(
    private authService: AuthService,
    private router: Router,
    private alertService: AlertService
  ) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        let message = 'Erro inesperado. Tente novamente.';

        if (error.status === 0) {
          message = 'Não foi possível conectar ao servidor. Verifique sua conexão.';
          this.alertService.toast(message, 'error');
        } else if (error.status === 401) {
          this.authService.logout();
          this.router.navigate(['/login']);
          message = 'Sessão expirada. Faça login novamente.';
          this.alertService.toast(message, 'warning');
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
      })
    );
  }
}
