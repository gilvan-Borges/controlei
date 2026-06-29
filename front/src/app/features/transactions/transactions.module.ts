import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { TransactionsRoutingModule } from './transactions-routing.module';
import { TransactionsListComponent } from './pages/transactions-list/transactions-list.component';
import { TransactionFormComponent } from './pages/transaction-form/transaction-form.component';

@NgModule({
  declarations: [
    TransactionsListComponent,
    TransactionFormComponent
  ],
  imports: [
    SharedModule,
    TransactionsRoutingModule
  ]
})
export class TransactionsModule {}
