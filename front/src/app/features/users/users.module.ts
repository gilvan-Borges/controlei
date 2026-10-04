import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { UsersRoutingModule } from './users-routing.module';
import { UsersListComponent } from './pages/users-list/users-list.component';
import { UserFormComponent } from './pages/user-form/user-form.component';

@NgModule({
  declarations: [
    UsersListComponent,
    UserFormComponent
  ],
  imports: [
    SharedModule,
    UsersRoutingModule
  ]
})
export class UsersModule {}
