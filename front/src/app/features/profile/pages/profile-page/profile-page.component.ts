import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../../../core/services/auth.service';
import { FamilyService } from '../../../../core/services/family.service';
import { User } from '../../../../core/models/user.model';
import { Family } from '../../../../core/models/family.model';

@Component({
  selector: 'app-profile-page',
  standalone: false,
  templateUrl: './profile-page.component.html',
  styleUrl: './profile-page.component.scss'
})
export class ProfilePageComponent implements OnInit {
  user: User | null = null;
  family: Family | null = null;
  loading = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private familyService: FamilyService
  ) {}

  ngOnInit(): void {
    this.user = this.authService.currentUser;
    this.loadFamily();
  }

  private loadFamily(): void {
    this.loading = true;
    this.familyService.getCurrentFamily().subscribe({
      next: (family) => {
        this.family = family;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Erro ao carregar dados da família.';
        this.loading = false;
      }
    });
  }

  get roleLabel(): string {
    return this.user?.role === 'RESPONSIBLE' ? 'Responsável' : 'Membro';
  }
}
