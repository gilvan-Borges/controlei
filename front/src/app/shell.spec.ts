import { TestBed } from '@angular/core/testing';
import { Router, RouterModule } from '@angular/router';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HTTP_INTERCEPTORS, HttpClient } from '@angular/common/http';
import { ShellComponent } from './layout/shell/shell.component';
import { AuthService } from './core/services/auth.service';
import { AuthGuard } from './core/guards/auth.guard';
import { AuthInterceptor } from './core/interceptors/auth.interceptor';
import { environment } from '../environments/environment';

describe('ShellComponent', () => {
  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [
        RouterModule.forRoot([]),
        HttpClientTestingModule
      ],
      declarations: [
        ShellComponent
      ],
      providers: [
        AuthService
      ]
    }).compileComponents();
  });

  it('should create the shell', () => {
    const fixture = TestBed.createComponent(ShellComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });

  it('should render Controlei title', () => {
    const fixture = TestBed.createComponent(ShellComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h5')?.textContent).toContain('Controlei');
  });

  it('should expose the main navigation items', () => {
    const fixture = TestBed.createComponent(ShellComponent);
    const component = fixture.componentInstance;
    expect(component.navItems.map(item => item.route)).toEqual([
      '/app/dashboard',
      '/app/transactions',
      '/app/debts',
      '/app/installments',
      '/app/investments',
      '/app/accounts',
      '/app/categories',
      '/app/users',
      '/app/profile'
    ]);
  });
});

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should save accessToken after login', () => {
    const mockResponse = {
      accessToken: 'test-jwt-token',
      tokenType: 'Bearer',
      user: { id: '1', name: 'Test', email: 'test@test.com', familyId: 'f1', role: 'MEMBER', active: true }
    };

    service.login('test@test.com', 'password123').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'test@test.com', password: 'password123' });
    req.flush(mockResponse);

    expect(service.getToken()).toBe('test-jwt-token');
    expect(service.isAuthenticated).toBe(true);
    expect(service.currentUser?.name).toBe('Test');
  });

  it('should clear token on logout', () => {
    localStorage.setItem('controlei_token', 'some-token');
    localStorage.setItem('controlei_user', '{"id":"1"}');

    service.logout();

    expect(service.getToken()).toBeNull();
    expect(service.isAuthenticated).toBe(false);
    expect(service.currentUser).toBeNull();
  });

  it('should load current user from /me endpoint', () => {
    const mockUser = { id: '1', name: 'API User', email: 'api@test.com', familyId: 'f1', role: 'MEMBER', active: true };

    service.loadCurrentUser().subscribe(user => {
      expect(user.name).toBe('API User');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/me`);
    expect(req.request.method).toBe('GET');
    req.flush(mockUser);

    expect(service.currentUser?.name).toBe('API User');
  });
});

describe('AuthGuard', () => {
  let guard: AuthGuard;
  let authService: AuthService;
  let router: Router;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [
        RouterModule.forRoot([]),
        HttpClientTestingModule
      ],
      providers: [AuthGuard, AuthService]
    });
    guard = TestBed.inject(AuthGuard);
    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should allow access when user is authenticated', () => {
    localStorage.setItem('controlei_token', 'valid-token');
    localStorage.setItem('controlei_user', '{"id":"1","name":"User"}');
    authService = TestBed.inject(AuthService);

    const result = guard.canActivate();
    expect(result).toBe(true);
  });

  it('should redirect to /login when user is not authenticated', () => {
    const result = guard.canActivate();
    expect(result).not.toBe(true);
    // Result is a UrlTree to /login
    expect(result.toString()).toBe('/login');
  });
});

describe('AuthInterceptor', () => {
  let httpMock: HttpTestingController;
  let http: HttpClient;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthService,
        {
          provide: HTTP_INTERCEPTORS,
          useClass: AuthInterceptor,
          multi: true
        }
      ]
    });
    httpMock = TestBed.inject(HttpTestingController);
    http = TestBed.inject(HttpClient);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should add Authorization header when token exists', () => {
    localStorage.setItem('controlei_token', 'my-jwt-token');

    http.get('/api/test').subscribe();

    const req = httpMock.expectOne('/api/test');
    expect(req.request.headers.has('Authorization')).toBe(true);
    expect(req.request.headers.get('Authorization')).toBe('Bearer my-jwt-token');
    req.flush({});
  });

  it('should not add Authorization header when no token', () => {
    http.get('/api/test').subscribe();

    const req = httpMock.expectOne('/api/test');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });
});
