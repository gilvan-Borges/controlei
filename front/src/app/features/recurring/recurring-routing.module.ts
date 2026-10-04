import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RecurringPageComponent } from './pages/recurring-page.component';

const routes: Routes = [
  {
    path: '',
    component: RecurringPageComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RecurringRoutingModule { }
