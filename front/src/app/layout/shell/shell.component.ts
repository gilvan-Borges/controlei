import { Component, HostListener, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { Notification } from '../../core/models/notification.model';

export interface NavGroup {
  title: string;
  items: {
    label: string;
    icon: string;
    route: string;
    badge?: string;
  }[];
}

@Component({
  selector: 'app-shell',
  standalone: false,
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss'
})
export class ShellComponent implements OnInit {
  isMobile = false;
  isDark = true;
  showNotificationsDrawer = false;
  unreadCount = 0;
  recentNotifications: Notification[] = [];
  currentUser: any = null;

  navGroups: NavGroup[] = [
    {
      title: 'Principal',
      items: [
        { label: 'Dashboard', icon: 'bi-grid-1x2-fill', route: '/app/dashboard' },
        { label: 'Transações', icon: 'bi-arrow-left-right', route: '/app/transactions' },
        { label: 'Contas', icon: 'bi-wallet2', route: '/app/accounts' },
        { label: 'Categorias', icon: 'bi-tags-fill', route: '/app/categories' }
      ]
    },
    {
      title: 'Crédito & Planejamento',
      items: [
        { label: 'Cartões & Faturas', icon: 'bi-credit-card-2-front-fill', route: '/app/cards' },
        { label: 'Orçamentos & Tetos', icon: 'bi-pie-chart-fill', route: '/app/budgets' },
        { label: 'Recorrentes & Assinaturas', icon: 'bi-arrow-repeat', route: '/app/recurring' }
      ]
    },
    {
      title: 'Metas & Coletivo',
      items: [
        { label: 'Metas & Cofrinhos', icon: 'bi-piggy-bank-fill', route: '/app/goals' },
        { label: 'Divisão de Contas', icon: 'bi-person-lines-fill', route: '/app/splits' }
      ]
    },
    {
      title: 'Patrimônio & Fiscal',
      items: [
        { label: 'Investimentos', icon: 'bi-graph-up-arrow', route: '/app/investments' },
        { label: 'Dívidas & Parcelas', icon: 'bi-bank2', route: '/app/debts' },
        { label: 'Relatórios & IRPF', icon: 'bi-file-earmark-spreadsheet-fill', route: '/app/reports' }
      ]
    },
    {
      title: 'Automação & Conexões',
      items: [
        { label: 'Scanner OCR Cupons', icon: 'bi-receipt-cutoff', route: '/app/receipts' },
        { label: 'Open Finance', icon: 'bi-shield-lock-fill', route: '/app/open-finance' }
      ]
    },
    {
      title: 'Família & Conta',
      items: [
        { label: 'Membros da Família', icon: 'bi-people-fill', route: '/app/users' },
        { label: 'Notificações', icon: 'bi-bell-fill', route: '/app/notifications' },
        { label: 'Meu Perfil', icon: 'bi-person-circle', route: '/app/profile' }
      ]
    }
  ];

  mobileNavItems = [
    { label: 'Início', icon: 'bi-grid-1x2-fill', route: '/app/dashboard' },
    { label: 'Transações', icon: 'bi-arrow-left-right', route: '/app/transactions' },
    { label: 'Orçamentos', icon: 'bi-pie-chart-fill', route: '/app/budgets' },
    { label: 'Metas', icon: 'bi-piggy-bank-fill', route: '/app/goals' },
    { label: 'Mais', icon: 'bi-three-dots', route: '/app/profile' }
  ];

  constructor(
    private authService: AuthService,
    private notificationService: NotificationService,
    private router: Router
  ) {
    this.checkScreen();
    this.initTheme();
  }

  ngOnInit(): void {
    this.currentUser = this.authService.currentUser;
    this.loadNotifications();
  }

  @HostListener('window:resize')
  onResize(): void {
    this.checkScreen();
  }

  loadNotifications(): void {
    this.notificationService.getUnreadCount().subscribe({
      next: (res) => (this.unreadCount = res.unreadCount),
      error: () => (this.unreadCount = 0)
    });

    this.notificationService.listNotifications(false).subscribe({
      next: (data) => (this.recentNotifications = data.slice(0, 5)),
      error: () => (this.recentNotifications = [])
    });
  }

  toggleNotifications(): void {
    this.showNotificationsDrawer = !this.showNotificationsDrawer;
    if (this.showNotificationsDrawer) {
      this.loadNotifications();
    }
  }

  markNotificationRead(id: string): void {
    this.notificationService.markAsRead(id).subscribe({
      next: () => {
        this.loadNotifications();
      }
    });
  }

  markAllNotificationsRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.loadNotifications();
      }
    });
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
    this.isMobile = window.innerWidth < 992;
  }
}
