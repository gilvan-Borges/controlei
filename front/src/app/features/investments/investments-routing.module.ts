import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { InvestmentsListComponent } from './pages/investments-list/investments-list.component';
import { InvestmentFormComponent } from './pages/investment-form/investment-form.component';

const routes: Routes = [
  { path: '', component: InvestmentsListComponent },
  { path: 'new', component: InvestmentFormComponent },
  { path: ':id/edit', component: InvestmentFormComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class InvestmentsRoutingModule { }
