import {
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  OnInit,
  Output
} from '@angular/core';
import { PeriodStateService } from '../../../core/services/period-state.service';

export interface MonthOption {
  value: number;
  label: string;
  shortLabel: string;
}

@Component({
  selector: 'app-period-selector',
  standalone: false,
  templateUrl: './period-selector.component.html',
  styleUrl: './period-selector.component.scss'
})
export class PeriodSelectorComponent implements OnInit {
  @Input() month: number = new Date().getMonth() + 1;
  @Input() year: number = new Date().getFullYear();
  @Input() syncWithGlobal = false;
  @Input() compact = false;
  @Input() showQuickNav = true;

  @Output() monthChange = new EventEmitter<number>();
  @Output() yearChange = new EventEmitter<number>();
  @Output() periodChange = new EventEmitter<{ month: number; year: number }>();

  isPickerOpen = false;

  readonly months: MonthOption[] = [
    { value: 1, label: 'Janeiro', shortLabel: 'Jan' },
    { value: 2, label: 'Fevereiro', shortLabel: 'Fev' },
    { value: 3, label: 'Março', shortLabel: 'Mar' },
    { value: 4, label: 'Abril', shortLabel: 'Abr' },
    { value: 5, label: 'Maio', shortLabel: 'Mai' },
    { value: 6, label: 'Junho', shortLabel: 'Jun' },
    { value: 7, label: 'Julho', shortLabel: 'Jul' },
    { value: 8, label: 'Agosto', shortLabel: 'Ago' },
    { value: 9, label: 'Setembro', shortLabel: 'Set' },
    { value: 10, label: 'Outubro', shortLabel: 'Out' },
    { value: 11, label: 'Novembro', shortLabel: 'Nov' },
    { value: 12, label: 'Dezembro', shortLabel: 'Dez' }
  ];

  constructor(
    private periodService: PeriodStateService,
    private elementRef: ElementRef
  ) {}

  ngOnInit(): void {
    if (this.syncWithGlobal) {
      this.month = this.periodService.month;
      this.year = this.periodService.year;
    }
  }

  get currentMonthLabel(): string {
    const found = this.months.find(m => m.value === this.month);
    return found ? found.label : 'Mês';
  }

  get isCurrentMonth(): boolean {
    const today = new Date();
    return this.month === today.getMonth() + 1 && this.year === today.getFullYear();
  }

  togglePicker(): void {
    this.isPickerOpen = !this.isPickerOpen;
  }

  selectMonth(m: number): void {
    this.month = m;
    this.emitChanges();
    this.isPickerOpen = false;
  }

  changeYear(delta: number): void {
    this.year += delta;
    this.emitChanges();
  }

  prevMonth(): void {
    if (this.month === 1) {
      this.month = 12;
      this.year -= 1;
    } else {
      this.month -= 1;
    }
    this.emitChanges();
  }

  nextMonth(): void {
    if (this.month === 12) {
      this.month = 1;
      this.year += 1;
    } else {
      this.month += 1;
    }
    this.emitChanges();
  }

  setToday(): void {
    const today = new Date();
    this.month = today.getMonth() + 1;
    this.year = today.getFullYear();
    this.emitChanges();
    this.isPickerOpen = false;
  }

  private emitChanges(): void {
    this.monthChange.emit(this.month);
    this.yearChange.emit(this.year);
    this.periodChange.emit({ month: this.month, year: this.year });

    if (this.syncWithGlobal) {
      this.periodService.setPeriod(this.month, this.year);
    }
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.isPickerOpen = false;
    }
  }
}
