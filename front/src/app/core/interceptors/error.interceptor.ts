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

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        let message = 'Erro inesperado. Tente novamente.';

        if (error.status === 0) {
          message = 'Não foi possível conectar ao servidor.';
        } else if (error.status === 401) {
          this.authService.logout();
          this.router.navigate(['/login']);
          message = 'Sessão expirada. Faça login novamente.';
        } else if (error.status === 403) {
          message = 'Você não tem permissão para esta ação.';
        } else if (error.status === 404) {
          message = 'Recurso não encontrado.';
        } else if (error.status === 422 || error.status === 400) {
          message = error.error?.message || 'Dados inválidos.';
        } else if (error.status >= 500) {
          message = 'Erro no servidor. Tente novamente mais tarde.';
        }

        return throwError(() => ({ status: error.status, message }));
      })
    );
  }
}
