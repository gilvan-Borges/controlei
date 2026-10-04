import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RecurringRoutingModule } from './recurring-routing.module';
import { RecurringPageComponent } from './pages/recurring-page.component';

@NgModule({
  declarations: [
    RecurringPageComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    RecurringRoutingModule
  ]
})
export class RecurringModule { }
