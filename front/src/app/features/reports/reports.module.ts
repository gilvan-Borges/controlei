import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportsRoutingModule } from './reports-routing.module';
import { ReportsPageComponent } from './pages/reports-page.component';
import { SharedModule } from '../../shared/shared.module';

@NgModule({
  declarations: [
    ReportsPageComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReportsRoutingModule,
    SharedModule
  ]
})
export class ReportsModule { }
