import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ActionButtonComponent } from './components/action-button/action-button.component';
import { MoneyInputComponent } from './components/money-input/money-input.component';
import { DateInputComponent } from './components/date-input/date-input.component';
import { UserSelectorComponent } from './components/user-selector/user-selector.component';
import { CategorySelectorComponent } from './components/category-selector/category-selector.component';
import { SummaryCardComponent } from './components/summary-card/summary-card.component';
import { ConfirmModalComponent } from './components/confirm-modal/confirm-modal.component';
import { EmptyStateComponent } from './components/empty-state/empty-state.component';
import { LoadingComponent } from './components/loading/loading.component';
import { ErrorMessageComponent } from './components/error-message/error-message.component';

const SHARED_COMPONENTS = [
  ActionButtonComponent,
  MoneyInputComponent,
  DateInputComponent,
  UserSelectorComponent,
  CategorySelectorComponent,
  SummaryCardComponent,
  ConfirmModalComponent,
  EmptyStateComponent,
  LoadingComponent,
  ErrorMessageComponent
];

@NgModule({
  declarations: SHARED_COMPONENTS,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule
  ],
  exports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    ...SHARED_COMPONENTS
  ]
})
export class SharedModule {}
