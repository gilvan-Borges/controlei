import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BudgetsRoutingModule } from './budgets-routing.module';
import { BudgetsPageComponent } from './pages/budgets-page.component';
import { SharedModule } from '../../shared/shared.module';

@NgModule({
  declarations: [
    BudgetsPageComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    BudgetsRoutingModule,
    SharedModule
  ]
})
export class BudgetsModule { }
