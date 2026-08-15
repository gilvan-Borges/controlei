import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OpenFinanceRoutingModule } from './open-finance-routing.module';
import { OpenFinancePageComponent } from './pages/open-finance-page.component';

@NgModule({
  declarations: [
    OpenFinancePageComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    OpenFinanceRoutingModule
  ]
})
export class OpenFinanceModule { }
