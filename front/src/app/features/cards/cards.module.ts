import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CardsRoutingModule } from './cards-routing.module';
import { CardsPageComponent } from './pages/cards-page.component';

@NgModule({
  declarations: [
    CardsPageComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    CardsRoutingModule
  ]
})
export class CardsModule { }
