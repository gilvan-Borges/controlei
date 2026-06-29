import { Component, HostListener } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-shell',
  standalone: false,
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss'
})
export class ShellComponent {
  isMobile = true;
  isDark = true;

  navItems = [
    { label: 'Inicio', icon: 'bi-house', route: '/app/dashboard' },
    { label: 'Transacoes', icon: 'bi-arrow-left-right', route: '/app/transactions' },
    { label: 'Dividas', icon: 'bi-credit-card', route: '/app/debts' },
    { label: 'Parcelas', icon: 'bi-calendar-check', route: '/app/installments' },
    { label: 'Investimentos', icon: 'bi-graph-up', route: '/app/investments' },
    { label: 'Contas', icon: 'bi-wallet2', route: '/app/accounts' },
    { label: 'Categorias', icon: 'bi-tags', route: '/app/categories' },
    { label: 'Membros', icon: 'bi-people', route: '/app/users' },
    { label: 'Perfil', icon: 'bi-person', route: '/app/profile' }
  ];

  mobileNavItems = this.navItems.filter(item =>
    ['/app/dashboard', '/app/transactions', '/app/debts', '/app/investments', '/app/profile'].includes(item.route)
  );

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    this.checkScreen();
    this.initTheme();
  }

  @HostListener('window:resize')
  onResize(): void {
    this.checkScreen();
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

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  private checkScreen(): void {
    this.isMobile = window.innerWidth < 768;
  }
}
