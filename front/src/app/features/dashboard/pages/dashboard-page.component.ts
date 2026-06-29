import { Component } from '@angular/core';

@Component({
  selector: 'app-dashboard-page',
  standalone: false,
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.scss'
})
export class DashboardPageComponent {
  currentMonth = new Date().toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
}
