import { Component } from '@angular/core';

@Component({
  selector: 'app-bottom-nav',
  standalone: false,
  templateUrl: './bottom-nav.component.html',
  styleUrl: './bottom-nav.component.scss'
})
export class BottomNavComponent {
  navItems = [
    { label: 'Início', icon: 'bi-house', route: '/app/dashboard' },
    { label: 'Transações', icon: 'bi-arrow-left-right', route: '/app/transactions' },
    { label: 'Dívidas', icon: 'bi-credit-card', route: '/app/debts' },
    { label: 'Investimentos', icon: 'bi-graph-up', route: '/app/investments' },
    { label: 'Perfil', icon: 'bi-person', route: '/app/profile' }
  ];
}
