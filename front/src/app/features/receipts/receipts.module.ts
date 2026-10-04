import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReceiptsRoutingModule } from './receipts-routing.module';
import { ReceiptsPageComponent } from './pages/receipts-page.component';

@NgModule({
  declarations: [
    ReceiptsPageComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReceiptsRoutingModule
  ]
})
export class ReceiptsModule { }
