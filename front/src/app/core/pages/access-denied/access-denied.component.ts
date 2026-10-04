import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-access-denied',
  standalone: false,
  templateUrl: './access-denied.component.html',
  styleUrl: './access-denied.component.scss'
})
export class AccessDeniedComponent {
  constructor(private router: Router) {}

  goDashboard(): void {
    this.router.navigate(['/app/dashboard']);
  }

  goBack(): void {
    window.history.back();
  }
}
