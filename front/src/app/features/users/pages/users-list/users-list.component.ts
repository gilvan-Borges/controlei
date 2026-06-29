import { Component, OnInit } from '@angular/core';
import { UserService } from '../../../../core/services/user.service';
import { PermissionHelper } from '../../../../core/services/permission.helper';
import { User } from '../../../../core/models/user.model';

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
    this.userService.listUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Erro ao carregar membros.';
        this.loading = false;
      }
    });
  }

  getRoleLabel(role: string): string {
    return role === 'RESPONSIBLE' ? 'Responsável' : 'Membro';
  }
}
