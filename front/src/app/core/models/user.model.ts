export interface User {
  id: string;
  familyId: string;
  name: string;
  email: string;
  role: 'RESPONSIBLE' | 'MEMBER';
  active: boolean;
}
