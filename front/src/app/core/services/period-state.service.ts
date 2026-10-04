import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface Period {
  month: number; // 1 - 12
  year: number;
}

@Injectable({
  providedIn: 'root'
})
export class PeriodStateService {
  private readonly now = new Date();
  private readonly initialPeriod: Period = {
    month: this.now.getMonth() + 1,
    year: this.now.getFullYear()
  };

  private readonly periodSubject = new BehaviorSubject<Period>(this.initialPeriod);
  public readonly period$: Observable<Period> = this.periodSubject.asObservable();

  get currentPeriod(): Period {
    return this.periodSubject.getValue();
  }

  get month(): number {
    return this.currentPeriod.month;
  }

  get year(): number {
    return this.currentPeriod.year;
  }

  setPeriod(month: number, year: number): void {
    this.periodSubject.next({ month, year });
  }

  setMonth(month: number): void {
    this.setPeriod(month, this.year);
  }

  setYear(year: number): void {
    this.setPeriod(this.month, year);
  }

  nextMonth(): void {
    let { month, year } = this.currentPeriod;
    if (month === 12) {
      month = 1;
      year += 1;
    } else {
      month += 1;
    }
    this.setPeriod(month, year);
  }

  prevMonth(): void {
    let { month, year } = this.currentPeriod;
    if (month === 1) {
      month = 12;
      year -= 1;
    } else {
      month -= 1;
    }
    this.setPeriod(month, year);
  }

  resetToCurrent(): void {
    const today = new Date();
    this.setPeriod(today.getMonth() + 1, today.getFullYear());
  }
}
