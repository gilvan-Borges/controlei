import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { InstallmentsRoutingModule } from './installments-routing.module';
import { InstallmentsListComponent } from './pages/installments-list/installments-list.component';

@NgModule({
  declarations: [
    InstallmentsListComponent
  ],
  imports: [
    SharedModule,
    InstallmentsRoutingModule
  ]
})
export class InstallmentsModule {}
