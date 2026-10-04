import { Component } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-desktop-shell',
  standalone: false,
  templateUrl: './desktop-shell.component.html',
  styleUrl: './desktop-shell.component.scss'
})
export class DesktopShellComponent {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
