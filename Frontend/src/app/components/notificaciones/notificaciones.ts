import { DatePipe, formatCurrency } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, LOCALE_ID, OnInit, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';

import { AuthService } from '../../services/auth.service';
import { ListaEsperaService } from '../../services/lista-espera.service';
import { PublicacionReventaService } from '../../services/publicacion-reventa.service';
import { mensajeError } from '../../utils/validaciones';

interface Aviso {
  id: string;
  fechaHora: string;
  mensaje: string;
  canal: string;
}

@Component({
  selector: 'app-notificaciones',
  imports: [DatePipe],
  template: `
    <div class="pagina-panel">
      <header class="panel-header">
        <h1>Notificaciones</h1>
        <p>Boletas que vendiste y avisos de tus listas de espera.</p>
      </header>

      @if (cargando()) {
        <div class="esqueleto" style="height: 200px"></div>
      } @else if (error()) {
        <div class="aviso aviso-error" role="alert">
          <strong>{{ error() }}</strong>
          <button type="button" class="btn btn-ghost" (click)="cargar()">Reintentar</button>
        </div>
      } @else if (avisos().length === 0) {
        <div class="aviso"><strong>No tienes notificaciones</strong></div>
      } @else {
        <table class="tabla">
          <tbody>
            @for (item of avisos(); track item.id) {
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
  private publicacionService = inject(PublicacionReventaService);
  private authService = inject(AuthService);
  private locale = inject(LOCALE_ID);

  protected cargando = signal(true);
  protected error = signal('');
  protected avisos = signal<Aviso[]>([]);

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    const documento = this.authService.sesion()?.documento ?? 0;

    this.cargando.set(true);
    this.error.set('');

    forkJoin({
      notificaciones: this.listaService.notificaciones(documento),
      ventas: this.publicacionService.ventas(documento),
    }).subscribe({
      next: ({ notificaciones, ventas }) => {
        const deListas: Aviso[] = notificaciones.map((n) => ({
          id: `n-${n.idNotificacion}`,
          fechaHora: n.fechaHora,
          mensaje: n.mensaje,
          canal: n.canal,
        }));
        const deVentas: Aviso[] = ventas.map((v) => ({
          id: `v-${v.idTransaccion}`,
          fechaHora: v.fechaHora,
          mensaje: `Vendiste tu boleta ${v.codigoBoleta} por ${formatCurrency(v.precio, this.locale, '$', 'COP', '1.0-0')}.`,
          canal: 'APP',
        }));

        this.avisos.set(
          [...deListas, ...deVentas].sort((a, b) => b.fechaHora.localeCompare(a.fechaHora)),
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
