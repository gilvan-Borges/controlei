import { Component, OnInit } from '@angular/core';
import { DashboardService } from '../services/dashboard.service';
import { IndividualDashboardResponse, FamilyDashboardResponse } from '../../../core/models/dashboard.model';

@Component({
  selector: 'app-dashboard-page',
  standalone: false,
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.scss'
})
export class DashboardPageComponent implements OnInit {
  view: 'individual' | 'family' = 'individual';
  loading = false;
  errorMessage = '';

  year: number;
  month: number;

  individualData: IndividualDashboardResponse | null = null;
  familyData: FamilyDashboardResponse | null = null;

  months = [
    { value: 1, label: 'Janeiro' },
    { value: 2, label: 'Fevereiro' },
    { value: 3, label: 'Março' },
    { value: 4, label: 'Abril' },
    { value: 5, label: 'Maio' },
    { value: 6, label: 'Junho' },
    { value: 7, label: 'Julho' },
    { value: 8, label: 'Agosto' },
    { value: 9, label: 'Setembro' },
    { value: 10, label: 'Outubro' },
    { value: 11, label: 'Novembro' },
    { value: 12, label: 'Dezembro' }
  ];

  constructor(private dashboardService: DashboardService) {
    const now = new Date();
    this.year = now.getFullYear();
    this.month = now.getMonth() + 1;
  }

  ngOnInit(): void {
    this.loadData();
  }

  switchView(view: 'individual' | 'family'): void {
    this.view = view;
    if (view === 'family' && !this.familyData) {
      this.loadData();
    } else if (view === 'individual' && !this.individualData) {
      this.loadData();
    }
  }

  onMonthChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.month = parseInt(select.value, 10);
    this.individualData = null;
    this.familyData = null;
    this.loadData();
  }

  onYearChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.year = parseInt(select.value, 10);
    this.individualData = null;
    this.familyData = null;
    this.loadData();
  }

  get monthLabel(): string {
    return this.months.find(m => m.value === this.month)?.label || '';
  }

  get currentBalance(): number {
    return (this.view === 'individual' ? this.individualData?.balance : this.familyData?.balance) || 0;
  }

  private loadData(): void {
    this.loading = true;
    this.errorMessage = '';

    if (this.view === 'individual') {
      this.dashboardService.getIndividual(this.year, this.month).subscribe({
        next: (data) => {
          this.individualData = data;
          this.loading = false;
        },
        error: (err) => {
          this.errorMessage = err?.message || 'Erro ao carregar dashboard.';
          this.loading = false;
        }
      });
    } else {
      this.dashboardService.getFamily(this.year, this.month).subscribe({
        next: (data) => {
          this.familyData = data;
          this.loading = false;
        },
        error: (err) => {
          this.errorMessage = err?.message || 'Erro ao carregar dashboard familiar.';
          this.loading = false;
        }
      });
    }
  }
}
