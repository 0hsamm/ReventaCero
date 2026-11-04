import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';

import { Notificacion } from '../../models/modelos';
import { AuthService } from '../../services/auth.service';
import { ListaEsperaService } from '../../services/lista-espera.service';
import { mensajeError } from '../../utils/validaciones';

@Component({
  selector: 'app-notificaciones',
  imports: [DatePipe],
  template: `
    <div class="pagina-panel">
      <header class="panel-header">
        <h1>Notificaciones</h1>
        <p>Avisos de tus listas de espera.</p>
      </header>

      @if (cargando()) {
        <div class="esqueleto" style="height: 200px"></div>
      } @else if (error()) {
        <div class="aviso aviso-error" role="alert">
          <strong>{{ error() }}</strong>
          <button type="button" class="btn btn-ghost" (click)="cargar()">Reintentar</button>
        </div>
      } @else if (notificaciones().length === 0) {
        <div class="aviso"><strong>No tienes notificaciones</strong></div>
      } @else {
        <table class="tabla">
          <tbody>
            @for (item of notificaciones(); track item.idNotificacion) {
              <tr>
                <td class="sin-salto">{{ item.fechaHora | date: 'd MMM y, HH:mm' }}</td>
                <td>{{ item.mensaje }}</td>
                <td>{{ item.canal }}</td>
              </tr>
            }
          </tbody>
        </table>
      }
    </div>
  `,
})
export class NotificacionesComponent implements OnInit {
  private listaService = inject(ListaEsperaService);
  private authService = inject(AuthService);

  protected cargando = signal(true);
  protected error = signal('');
  protected notificaciones = signal<Notificacion[]>([]);

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    this.listaService.notificaciones(this.authService.sesion()?.documento ?? 0).subscribe({
      next: (lista) => {
        this.notificaciones.set(
          [...lista].sort((a, b) => b.fechaHora.localeCompare(a.fechaHora)),
        );
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(mensajeError(err, 'No pudimos cargar tus notificaciones'));
        this.cargando.set(false);
      },
    });
  }
}
