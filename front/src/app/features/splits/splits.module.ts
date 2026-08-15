import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SplitsRoutingModule } from './splits-routing.module';
import { SplitsPageComponent } from './pages/splits-page.component';

@NgModule({
  declarations: [
    SplitsPageComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    SplitsRoutingModule
  ]
})
export class SplitsModule { }
