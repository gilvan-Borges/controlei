import { Injectable } from '@angular/core';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class PermissionHelper {
  constructor(private authService: AuthService) {}

  isResponsible(): boolean {
    return this.authService.currentUser?.role === 'RESPONSIBLE';
  }

  isMember(): boolean {
    return this.authService.currentUser?.role === 'MEMBER';
  }

  canEditRecord(recordUserId: string): boolean {
    if (this.isResponsible()) {
      return true;
    }
    return this.authService.currentUser?.id === recordUserId;
  }

  canCreateMember(): boolean {
    return this.isResponsible();
  }

  canEditMember(): boolean {
    return this.isResponsible();
  }

  getCurrentUserId(): string | null {
    return this.authService.currentUser?.id || null;
  }

  getCurrentFamilyId(): string | null {
    return this.authService.currentUser?.familyId || null;
  }
}
