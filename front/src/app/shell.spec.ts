import { TestBed } from '@angular/core/testing';
import { RouterModule } from '@angular/router';
import { MobileShellComponent } from './layout/mobile-shell/mobile-shell.component';
import { BottomNavComponent } from './layout/bottom-nav/bottom-nav.component';
import { AuthService } from './core/services/auth.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('MobileShellComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RouterModule.forRoot([]),
        HttpClientTestingModule
      ],
      declarations: [
        MobileShellComponent,
        BottomNavComponent
      ],
      providers: [
        AuthService
      ]
    }).compileComponents();
  });

  it('should create the shell', () => {
    const fixture = TestBed.createComponent(MobileShellComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });

  it('should render the header with title', () => {
    const fixture = TestBed.createComponent(MobileShellComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h5')?.textContent).toContain('Controlei');
  });

  it('should render bottom navigation', () => {
    const fixture = TestBed.createComponent(MobileShellComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('app-bottom-nav')).toBeTruthy();
  });
});
