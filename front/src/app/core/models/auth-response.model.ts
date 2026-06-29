import { User } from './user.model';

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  user: User;
}
