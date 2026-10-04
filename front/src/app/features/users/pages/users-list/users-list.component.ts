import { Component, OnInit } from '@angular/core';
import { finalize, take } from 'rxjs/operators';
import { User } from '../../../../core/models/user.model';
import { PermissionHelper } from '../../../../core/services/permission.helper';
import { UserService } from '../../../../core/services/user.service';

@Component({
  selector: 'app-users-list',
  standalone: false,
  templateUrl: './users-list.component.html',
  styleUrl: './users-list.component.scss'
})
export class UsersListComponent implements OnInit {
  users: User[] = [];
  loading = false;
  errorMessage = '';

  constructor(
    private userService: UserService,
    public permissions: PermissionHelper
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.errorMessage = '';

    this.userService.listUsers().pipe(
      take(1),
      finalize(() => {
        this.loading = false;
      })
    ).subscribe({
      next: (users) => {
        this.users = Array.isArray(users) ? users : [];
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Erro ao carregar membros.';
      }
    });
  }

  getRoleLabel(role: string): string {
    return role === 'RESPONSIBLE' ? 'Responsavel' : 'Membro';
  }
}
