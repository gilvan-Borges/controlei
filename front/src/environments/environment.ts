import type { DemoLogin } from './environment.types';

export const environment = {
  production: false,
  apiUrl: '/api/v1',
  // Contas de demonstracao do seed LOCAL (APP_SEED_ENABLED). Existem so neste arquivo: o build de producao troca
  // o arquivo por environment.prod.ts, entao estas credenciais nunca entram no pacote publicado.
  demoLogin: {
    admin: { email: 'superadmin@controlei.local', password: 'Controlei@123' },
    member: { email: 'gilvan.borges@controlei.local', password: 'Controlei@123' }
  } as DemoLogin | null
};
