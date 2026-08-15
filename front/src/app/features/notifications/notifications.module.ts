import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NotificationsRoutingModule } from './notifications-routing.module';
import { NotificationsPageComponent } from './pages/notifications-page.component';

@NgModule({
  declarations: [
    NotificationsPageComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    NotificationsRoutingModule
  ]
})
export class NotificationsModule { }
