export type AuditAction = 'CREATE' | 'UPDATE' | 'DELETE';

export interface AuditLog {
  id: string;
  familyId: string;
  userId?: string;
  userName?: string;
  entityName: string;
  entityId: string;
  action: AuditAction;
  oldValue?: string;
  newValue?: string;
  ipAddress?: string;
  userAgent?: string;
  createdAt: string;
}
