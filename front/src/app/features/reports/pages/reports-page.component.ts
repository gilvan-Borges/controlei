import { Component, OnInit } from '@angular/core';
import { ReportService } from '../../../core/services/report.service';
import { TaxDeclarationReport } from '../../../core/models/report.model';

@Component({
  selector: 'app-reports-page',
  standalone: false,
  templateUrl: './reports-page.component.html',
  styleUrl: './reports-page.component.scss'
})
export class ReportsPageComponent implements OnInit {
  selectedYear = new Date().getFullYear();
  selectedMonth = new Date().getMonth() + 1;
  taxYear = new Date().getFullYear();

  taxReport: TaxDeclarationReport | null = null;
  loadingTax = false;
  downloadingCsv = false;
  errorMessage = '';
  successMessage = '';

  months = [
    { value: 1, label: 'Janeiro' }, { value: 2, label: 'Fevereiro' },
    { value: 3, label: 'Março' }, { value: 4, label: 'Abril' },
    { value: 5, label: 'Maio' }, { value: 6, label: 'Junho' },
    { value: 7, label: 'Julho' }, { value: 8, label: 'Agosto' },
    { value: 9, label: 'Setembro' }, { value: 10, label: 'Outubro' },
    { value: 11, label: 'Novembro' }, { value: 12, label: 'Dezembro' }
  ];

  constructor(private reportService: ReportService) {}

  ngOnInit(): void {
    this.loadTaxDeclaration();
  }

  onPeriodSelect(period: { month: number; year: number }): void {
    this.selectedMonth = period.month;
    this.selectedYear = period.year;
  }

  loadTaxDeclaration(): void {
    this.loadingTax = true;
    this.errorMessage = '';
    this.reportService.getTaxDeclaration(this.taxYear).subscribe({
      next: (data) => {
        this.taxReport = data;
        this.loadingTax = false;
      },
      error: (err) => {
        this.errorMessage = 'Erro ao carregar relatório IRPF: ' + (err.error?.message || err.message);
        this.loadingTax = false;
      }
    });
  }

  downloadCsv(): void {
    this.downloadingCsv = true;
    this.reportService.downloadMonthlyStatementCsv(this.selectedYear, this.selectedMonth).subscribe({
      next: (blob) => {
        this.downloadingCsv = false;
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `extrato-mensal-${this.selectedYear}-${this.selectedMonth.toString().padStart(2, '0')}.csv`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.successMessage = 'Extrato CSV baixado com sucesso!';
        setTimeout(() => (this.successMessage = ''), 4000);
      },
      error: (err) => {
        this.downloadingCsv = false;
        this.errorMessage = 'Erro ao baixar CSV: ' + (err.error?.message || err.message);
      }
    });
  }
}
