import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BudgetsRoutingModule } from './budgets-routing.module';
import { BudgetsPageComponent } from './pages/budgets-page.component';

@NgModule({
  declarations: [
    BudgetsPageComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    BudgetsRoutingModule
  ]
})
export class BudgetsModule { }
