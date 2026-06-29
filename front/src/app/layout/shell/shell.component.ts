import { Component, HostListener } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-shell',
  standalone: false,
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss'
})
export class ShellComponent {
  isMobile = true;

  navItems = [
    { label: 'Início', icon: 'bi-house', route: '/app/dashboard' },
    { label: 'Transações', icon: 'bi-arrow-left-right', route: '/app/transactions' },
    { label: 'Dívidas', icon: 'bi-credit-card', route: '/app/debts' },
    { label: 'Parcelas', icon: 'bi-calendar-check', route: '/app/installments' },
    { label: 'Investimentos', icon: 'bi-graph-up', route: '/app/investments' },
    { label: 'Contas', icon: 'bi-wallet2', route: '/app/accounts' },
    { label: 'Categorias', icon: 'bi-tags', route: '/app/categories' },
    { label: 'Membros', icon: 'bi-people', route: '/app/users' },
    { label: 'Perfil', icon: 'bi-person', route: '/app/profile' }
  ];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    this.checkScreen();
  }

  @HostListener('window:resize')
  onResize(): void {
    this.checkScreen();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  private checkScreen(): void {
    this.isMobile = window.innerWidth < 768;
  }
}
