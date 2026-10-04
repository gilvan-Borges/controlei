import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { DashboardService } from './features/dashboard/services/dashboard.service';
import { environment } from '../environments/environment';
import { DashboardPageComponent } from './features/dashboard/pages/dashboard-page.component';
import { SharedModule } from './shared/shared.module';

describe('DashboardService', () => {
  let service: DashboardService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [DashboardService]
    });
    service = TestBed.inject(DashboardService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call GET /dashboard/individual with startDate and endDate', () => {
    service.getIndividual(2026, 6).subscribe();

    const req = httpMock.expectOne(
      r => r.url === `${environment.apiUrl}/dashboard/individual`
    );
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('startDate')).toBe('2026-06-01');
    expect(req.request.params.get('endDate')).toBe('2026-06-30');
    req.flush({ totalIncome: 0, totalExpense: 0, balance: 0 });
  });

  it('should call GET /dashboard/family with startDate and endDate', () => {
    service.getFamily(2026, 2).subscribe();

    const req = httpMock.expectOne(
      r => r.url === `${environment.apiUrl}/dashboard/family`
    );
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('startDate')).toBe('2026-02-01');
    expect(req.request.params.get('endDate')).toBe('2026-02-28');
    req.flush({ totalIncome: 0, totalExpense: 0, balance: 0, userDetails: [] });
  });

  it('should call without params when year/month not provided', () => {
    service.getIndividual().subscribe();

    const req = httpMock.expectOne(
      r => r.url === `${environment.apiUrl}/dashboard/individual`
    );
    expect(req.request.params.keys().length).toBe(0);
    req.flush({ totalIncome: 0 });
  });
});

describe('DashboardPageComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, SharedModule],
      declarations: [DashboardPageComponent],
      providers: [DashboardService],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(DashboardPageComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should default to individual view', () => {
    const fixture = TestBed.createComponent(DashboardPageComponent);
    expect(fixture.componentInstance.view).toBe('individual');
  });

  it('should switch to family view', () => {
    const fixture = TestBed.createComponent(DashboardPageComponent);
    const component = fixture.componentInstance;
    component.switchView('family');
    expect(component.view).toBe('family');
  });

  it('should render view toggle buttons', () => {
    const fixture = TestBed.createComponent(DashboardPageComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Individual');
    expect(compiled.textContent).toContain('Familiar');
  });

  it('should render period selectors', () => {
    const fixture = TestBed.createComponent(DashboardPageComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    const selects = compiled.querySelectorAll('select');
    expect(selects.length).toBeGreaterThanOrEqual(2);
  });
});
