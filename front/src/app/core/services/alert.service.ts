import { Injectable } from '@angular/core';
import Swal, { SweetAlertIcon, SweetAlertResult } from 'sweetalert2';

@Injectable({
  providedIn: 'root'
})
export class AlertService {
  private isDarkMode(): boolean {
    return document.documentElement.getAttribute('data-theme') !== 'light';
  }

  private getThemeConfig() {
    const isDark = this.isDarkMode();
    return {
      background: isDark ? '#18181b' : '#ffffff',
      color: isDark ? '#ffffff' : '#09090b',
      confirmButtonColor: '#10b981',
      cancelButtonColor: isDark ? '#27272a' : '#e4e4e7',
      customClass: {
        popup: 'controlei-swal-popup',
        title: 'controlei-swal-title',
        htmlContainer: 'controlei-swal-html',
        confirmButton: 'btn btn-primary',
        cancelButton: 'btn btn-secondary'
      },
      buttonsStyling: false
    };
  }

  /**
   * Mostra alerta de sucesso
   */
  success(title: string, message?: string): Promise<SweetAlertResult> {
    const cfg = this.getThemeConfig();
    return Swal.fire({
      icon: 'success',
      title,
      text: message,
      background: cfg.background,
      color: cfg.color,
      confirmButtonText: 'OK',
      customClass: cfg.customClass,
      buttonsStyling: cfg.buttonsStyling
    });
  }

  /**
   * Mostra alerta de erro
   */
  error(title: string, message?: string): Promise<SweetAlertResult> {
    const cfg = this.getThemeConfig();
    return Swal.fire({
      icon: 'error',
      title,
      text: message,
      background: cfg.background,
      color: cfg.color,
      confirmButtonText: 'Fechar',
      customClass: {
        ...cfg.customClass,
        confirmButton: 'btn btn-danger'
      },
      buttonsStyling: cfg.buttonsStyling
    });
  }

  /**
   * Mostra alerta de aviso / atenção
   */
  warning(title: string, message?: string): Promise<SweetAlertResult> {
    const cfg = this.getThemeConfig();
    return Swal.fire({
      icon: 'warning',
      title,
      text: message,
      background: cfg.background,
      color: cfg.color,
      confirmButtonText: 'Entendido',
      customClass: cfg.customClass,
      buttonsStyling: cfg.buttonsStyling
    });
  }

  /**
   * Mostra alerta informativo
   */
  info(title: string, message?: string): Promise<SweetAlertResult> {
    const cfg = this.getThemeConfig();
    return Swal.fire({
      icon: 'info',
      title,
      text: message,
      background: cfg.background,
      color: cfg.color,
      confirmButtonText: 'OK',
      customClass: cfg.customClass,
      buttonsStyling: cfg.buttonsStyling
    });
  }

  /**
   * Diálogo de confirmação para ações importantes (ex: salvar, atualizar)
   */
  async confirm(
    title: string,
    message: string,
    confirmText = 'Sim, confirmar',
    cancelText = 'Cancelar'
  ): Promise<boolean> {
    const cfg = this.getThemeConfig();
    const result = await Swal.fire({
      icon: 'question',
      title,
      text: message,
      showCancelButton: true,
      confirmButtonText: confirmText,
      cancelButtonText: cancelText,
      background: cfg.background,
      color: cfg.color,
      customClass: cfg.customClass,
      buttonsStyling: cfg.buttonsStyling
    });
    return result.isConfirmed;
  }

  /**
   * Diálogo de confirmação para exclusões críticas
   */
  async deleteConfirm(
    itemLabel: string,
    extraWarning = 'Esta ação não poderá ser desfeita.'
  ): Promise<boolean> {
    const cfg = this.getThemeConfig();
    const result = await Swal.fire({
      icon: 'warning',
      title: `Excluir ${itemLabel}?`,
      text: extraWarning,
      showCancelButton: true,
      confirmButtonText: 'Sim, excluir',
      cancelButtonText: 'Cancelar',
      background: cfg.background,
      color: cfg.color,
      customClass: {
        ...cfg.customClass,
        confirmButton: 'btn btn-danger'
      },
      buttonsStyling: cfg.buttonsStyling
    });
    return result.isConfirmed;
  }

  /**
   * Notificação rápida em Toast no canto superior direito
   */
  toast(title: string, icon: SweetAlertIcon = 'success'): void {
    const isDark = this.isDarkMode();
    const Toast = Swal.mixin({
      toast: true,
      position: 'top-end',
      showConfirmButton: false,
      timer: 3500,
      timerProgressBar: true,
      background: isDark ? '#18181b' : '#ffffff',
      color: isDark ? '#ffffff' : '#09090b',
      didOpen: (toast) => {
        toast.onmouseenter = Swal.stopTimer;
        toast.onmouseleave = Swal.resumeTimer;
      }
    });

    Toast.fire({
      icon,
      title
    });
  }
}
