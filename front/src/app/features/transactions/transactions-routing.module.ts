import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TransactionsListComponent } from './pages/transactions-list/transactions-list.component';
import { TransactionFormComponent } from './pages/transaction-form/transaction-form.component';

const routes: Routes = [
  { path: '', component: TransactionsListComponent },
  { path: 'new-income', component: TransactionFormComponent, data: { type: 'INCOME' } },
  { path: 'new-expense', component: TransactionFormComponent, data: { type: 'EXPENSE' } },
  { path: ':id/edit', component: TransactionFormComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TransactionsRoutingModule { }
