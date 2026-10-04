import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AccountsListComponent } from './pages/accounts-list/accounts-list.component';
import { AccountFormComponent } from './pages/account-form/account-form.component';

const routes: Routes = [
  { path: '', component: AccountsListComponent },
  { path: 'new', component: AccountFormComponent },
  { path: ':id/edit', component: AccountFormComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AccountsRoutingModule { }
