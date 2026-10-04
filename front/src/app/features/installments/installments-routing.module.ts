import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { InstallmentsListComponent } from './pages/installments-list/installments-list.component';

const routes: Routes = [
  { path: '', component: InstallmentsListComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class InstallmentsRoutingModule { }
