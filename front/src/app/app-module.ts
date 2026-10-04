import { NgModule, isDevMode, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
import { ServiceWorkerModule } from '@angular/service-worker';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { CoreModule } from './core/core.module';
import { AuthModule } from './core/auth/auth.module';
import { NotFoundComponent } from './core/pages/not-found/not-found.component';
import { AccessDeniedComponent } from './core/pages/access-denied/access-denied.component';

@NgModule({
  declarations: [
    App,
    NotFoundComponent,
    AccessDeniedComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    AppRoutingModule,
    CoreModule,
    AuthModule,
    // Service worker so em producao (em `ng serve` ele atrapalharia o recarregamento). Registra depois que o app
    // estabiliza, para nao competir com a primeira carga.
    ServiceWorkerModule.register('ngsw-worker.js', {
      enabled: !isDevMode(),
      registrationStrategy: 'registerWhenStable:30000'
    })
  ],
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
  ],
  bootstrap: [App]
})
export class AppModule {}
