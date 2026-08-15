import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { OpenFinancePageComponent } from './pages/open-finance-page.component';

const routes: Routes = [
  {
    path: '',
    component: OpenFinancePageComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class OpenFinanceRoutingModule { }
