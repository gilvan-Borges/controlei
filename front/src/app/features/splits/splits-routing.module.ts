import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SplitsPageComponent } from './pages/splits-page.component';

const routes: Routes = [
  {
    path: '',
    component: SplitsPageComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SplitsRoutingModule { }
