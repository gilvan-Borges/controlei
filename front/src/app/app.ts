import { Component, OnInit, inject } from '@angular/core';
import { SwUpdate, VersionReadyEvent } from '@angular/service-worker';
import { filter } from 'rxjs/operators';
import { AlertService } from './core/services/alert.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  standalone: false,
  styleUrl: './app.scss'
})
export class App implements OnInit {
  private readonly alerts = inject(AlertService);
  private readonly updates = inject(SwUpdate, { optional: true });

  ngOnInit(): void {
    // O service worker baixa a versao nova em segundo plano; ela so passa a valer ao recarregar. Avisar evita a pessoa
    // ficar dias numa versao antiga do app instalado.
    this.updates?.versionUpdates
      .pipe(filter((e): e is VersionReadyEvent => e.type === 'VERSION_READY'))
      .subscribe(() => this.alerts.toast('Nova versão do Controlei disponível. Feche e abra o app para atualizar.', 'info'));

    // O app abre offline (a "casca" fica em cache), mas os dados vem da API: dizer isso evita telas vazias sem explicacao.
    window.addEventListener('offline', () => this.alerts.toast('Você está offline. Os dados voltam quando a conexão voltar.', 'warning'));
    window.addEventListener('online', () => this.alerts.toast('Conexão restabelecida.', 'success'));
  }
}
