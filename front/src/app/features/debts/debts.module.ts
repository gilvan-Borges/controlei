import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { DebtsRoutingModule } from './debts-routing.module';
import { DebtsListComponent } from './pages/debts-list/debts-list.component';
import { DebtFormComponent } from './pages/debt-form/debt-form.component';
import { DebtDetailComponent } from './pages/debt-detail/debt-detail.component';

@NgModule({
  declarations: [
    DebtsListComponent,
    DebtFormComponent,
    DebtDetailComponent
  ],
  imports: [
    SharedModule,
    DebtsRoutingModule
  ]
})
export class DebtsModule {}
