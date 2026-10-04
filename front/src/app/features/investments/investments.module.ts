import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { InvestmentsRoutingModule } from './investments-routing.module';
import { InvestmentsListComponent } from './pages/investments-list/investments-list.component';
import { InvestmentFormComponent } from './pages/investment-form/investment-form.component';

@NgModule({
  declarations: [
    InvestmentsListComponent,
    InvestmentFormComponent
  ],
  imports: [
    SharedModule,
    InvestmentsRoutingModule
  ]
})
export class InvestmentsModule {}
