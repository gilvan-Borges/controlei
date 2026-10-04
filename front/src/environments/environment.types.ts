/** Tipos compartilhados pelos arquivos de ambiente (nao podem morar em environment.ts, que e trocado no build). */
export interface DemoAccount {
  email: string;
  password: string;
}

export interface DemoLogin {
  admin: DemoAccount;
  member: DemoAccount;
}
