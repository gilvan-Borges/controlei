import type { DemoLogin } from './environment.types';

export const environment = {
  production: true,
  apiUrl: '/api/v1',
  // Sem contas de demonstracao em producao
  demoLogin: null as DemoLogin | null
};
