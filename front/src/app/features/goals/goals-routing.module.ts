import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GoalsPageComponent } from './pages/goals-page.component';

const routes: Routes = [
  {
    path: '',
    component: GoalsPageComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class GoalsRoutingModule { }
