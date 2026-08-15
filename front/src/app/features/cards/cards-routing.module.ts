import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CardsPageComponent } from './pages/cards-page.component';

const routes: Routes = [
  {
    path: '',
    component: CardsPageComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CardsRoutingModule { }
