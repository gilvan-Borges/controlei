import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GoalsRoutingModule } from './goals-routing.module';
import { GoalsPageComponent } from './pages/goals-page.component';

@NgModule({
  declarations: [
    GoalsPageComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    GoalsRoutingModule
  ]
})
export class GoalsModule { }
