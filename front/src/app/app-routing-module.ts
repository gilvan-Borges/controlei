import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { ShellComponent } from './layout/shell/shell.component';
import { LoginComponent } from './core/auth/login.component';
import { RegisterComponent } from './core/auth/register.component';

const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'register',
    component: RegisterComponent
  },
  {
    path: 'app',
    component: ShellComponent,
    canActivate: [AuthGuard],
    children: [
      {
        path: 'dashboard',
        loadChildren: () => import('./features/dashboard/dashboard.module').then(m => m.DashboardModule)
      },
      {
        path: 'transactions',
        loadChildren: () => import('./features/transactions/transactions.module').then(m => m.TransactionsModule)
      },
      {
        path: 'debts',
        loadChildren: () => import('./features/debts/debts.module').then(m => m.DebtsModule)
      },
      {
        path: 'installments',
        loadChildren: () => import('./features/installments/installments.module').then(m => m.InstallmentsModule)
      },
      {
        path: 'investments',
        loadChildren: () => import('./features/investments/investments.module').then(m => m.InvestmentsModule)
      },
      {
        path: 'accounts',
        loadChildren: () => import('./features/accounts/accounts.module').then(m => m.AccountsModule)
      },
      {
        path: 'categories',
        loadChildren: () => import('./features/categories/categories.module').then(m => m.CategoriesModule)
      },
      {
        path: 'users',
        loadChildren: () => import('./features/users/users.module').then(m => m.UsersModule)
      },
      {
        path: 'budgets',
        loadChildren: () => import('./features/budgets/budgets.module').then(m => m.BudgetsModule)
      },
      {
        path: 'goals',
        loadChildren: () => import('./features/goals/goals.module').then(m => m.GoalsModule)
      },
      {
        path: 'cards',
        loadChildren: () => import('./features/cards/cards.module').then(m => m.CardsModule)
      },
      {
        path: 'splits',
        loadChildren: () => import('./features/splits/splits.module').then(m => m.SplitsModule)
      },
      {
        path: 'recurring',
        loadChildren: () => import('./features/recurring/recurring.module').then(m => m.RecurringModule)
      },
      {
        path: 'receipts',
        loadChildren: () => import('./features/receipts/receipts.module').then(m => m.ReceiptsModule)
      },
      {
        path: 'reports',
        loadChildren: () => import('./features/reports/reports.module').then(m => m.ReportsModule)
      },
      {
        path: 'open-finance',
        loadChildren: () => import('./features/open-finance/open-finance.module').then(m => m.OpenFinanceModule)
      },
      {
        path: 'notifications',
        loadChildren: () => import('./features/notifications/notifications.module').then(m => m.NotificationsModule)
      },
      {
        path: 'profile',
        loadChildren: () => import('./features/profile/profile.module').then(m => m.ProfileModule)
      },
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
