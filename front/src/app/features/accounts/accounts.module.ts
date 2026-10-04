import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { AccountsRoutingModule } from './accounts-routing.module';
import { AccountsListComponent } from './pages/accounts-list/accounts-list.component';
import { AccountFormComponent } from './pages/account-form/account-form.component';

@NgModule({
  declarations: [
    AccountsListComponent,
    AccountFormComponent
  ],
  imports: [
    SharedModule,
    AccountsRoutingModule
  ]
})
export class AccountsModule {}
