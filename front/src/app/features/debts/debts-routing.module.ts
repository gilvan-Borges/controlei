import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DebtsListComponent } from './pages/debts-list/debts-list.component';
import { DebtFormComponent } from './pages/debt-form/debt-form.component';
import { DebtDetailComponent } from './pages/debt-detail/debt-detail.component';

const routes: Routes = [
  { path: '', component: DebtsListComponent },
  { path: 'new', component: DebtFormComponent },
  { path: ':id', component: DebtDetailComponent },
  { path: ':id/edit', component: DebtFormComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DebtsRoutingModule { }
