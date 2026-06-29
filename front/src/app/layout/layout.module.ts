import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ShellComponent } from './shell/shell.component';
import { MobileShellComponent } from './mobile-shell/mobile-shell.component';
import { DesktopShellComponent } from './desktop-shell/desktop-shell.component';
import { BottomNavComponent } from './bottom-nav/bottom-nav.component';

@NgModule({
  declarations: [
    ShellComponent,
    MobileShellComponent,
    DesktopShellComponent,
    BottomNavComponent
  ],
  imports: [
    CommonModule,
    RouterModule
  ],
  exports: [
    ShellComponent,
    MobileShellComponent,
    DesktopShellComponent,
    BottomNavComponent
  ]
})
export class LayoutModule { }
