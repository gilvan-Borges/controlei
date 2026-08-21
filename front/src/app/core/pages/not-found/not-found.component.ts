import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-not-found',
  standalone: false,
  templateUrl: './not-found.component.html',
  styleUrl: './not-found.component.scss'
})
export class NotFoundComponent {
  constructor(private router: Router) {}

  goDashboard(): void {
    this.router.navigate(['/app/dashboard']);
  }

  goBack(): void {
    window.history.back();
  }
}
