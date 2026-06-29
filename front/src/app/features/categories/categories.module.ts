import { NgModule } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { CategoriesRoutingModule } from './categories-routing.module';
import { CategoriesListComponent } from './pages/categories-list/categories-list.component';
import { CategoryFormComponent } from './pages/category-form/category-form.component';

@NgModule({
  declarations: [
    CategoriesListComponent,
    CategoryFormComponent
  ],
  imports: [
    SharedModule,
    CategoriesRoutingModule
  ]
})
export class CategoriesModule {}
