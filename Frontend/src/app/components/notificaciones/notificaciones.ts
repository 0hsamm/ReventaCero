import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';

import { Notificacion, SolicitudReventa } from '../../models/modelos';
import { AuthService } from '../../services/auth.service';
import { ListaEsperaService } from '../../services/lista-espera.service';
import { SolicitudReventaService } from '../../services/solicitud-reventa.service';
import { ToastService } from '../../services/toast.service';
import { mensajeError } from '../../utils/validaciones';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog';

interface Respuesta {
  solicitud: SolicitudReventa;
  aceptar: boolean;
}

@Component({
  selector: 'app-notificaciones',
  imports: [ConfirmDialogComponent, CurrencyPipe, DatePipe],
  template: `
    <div class="pagina-panel">
      <header class="panel-header">
        <h1>Notificaciones</h1>
        <p>Ofertas de reventa que te hicieron y avisos de tus listas de espera.</p>
      </header>

      @if (cargando()) {
        <div class="esqueleto" style="height: 200px"></div>
      } @else if (error()) {
        <div class="aviso aviso-error" role="alert">
          <strong>{{ error() }}</strong>
          <button type="button" class="btn btn-ghost" (click)="cargar()">Reintentar</button>
        </div>
      } @else if (notificaciones().length === 0 && pendientes().length === 0) {
        <div class="aviso"><strong>No tienes notificaciones</strong></div>
      } @else {
        @for (oferta of pendientes(); track oferta.idSolicitud) {
          <section class="tarjeta-panel">
            <h2>{{ oferta.nombreVendedor }} te ofrece una boleta</h2>
            <dl class="oferta">
              <div>
                <dt>Festival</dt>
                <dd>{{ oferta.nombreFestival }}</dd>
              </div>
              <div>
                <dt>Entrada</dt>
                <dd>{{ oferta.categoriaEntrada }} · {{ oferta.nombreZona }}</dd>
              </div>
              <div>
                <dt>Precio</dt>
                <dd>{{ oferta.precio | currency: 'COP' : 'symbol-narrow' : '1.0-0' }}</dd>
              </div>
              <div>
                <dt>Método de pago</dt>
                <dd>{{ oferta.metodoPago }}</dd>
              </div>
              <div>
                <dt>Recibida</dt>
                <dd>{{ oferta.fechaHora | date: 'd MMM y, HH:mm' }}</dd>
              </div>
            </dl>
            <div class="acciones-fila">
              <button
                type="button"
                class="btn btn-primary"
                [disabled]="enviando()"
                (click)="respuesta.set({ solicitud: oferta, aceptar: true })"
              >
                Aceptar
              </button>
              <button
                type="button"
                class="btn btn-ghost"
                [disabled]="enviando()"
                (click)="respuesta.set({ solicitud: oferta, aceptar: false })"
              >
                Rechazar
              </button>
            </div>
          </section>
        }

        @if (notificaciones().length) {
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
      }
    </div>

    <app-confirm-dialog
      [title]="respuesta()?.aceptar ? 'Aceptar reventa' : 'Rechazar oferta'"
      [message]="mensajeConfirmacion()"
      [confirmLabel]="respuesta()?.aceptar ? 'Aceptar' : 'Rechazar'"
      [visible]="respuesta() != null"
      (confirmed)="responder()"
      (cancelled)="respuesta.set(null)"
    ></app-confirm-dialog>
  `,
  styles: `
    .oferta {
      display: flex;
      flex-wrap: wrap;
      gap: 12px 40px;
      margin: 0 0 20px;
    }

    dt {
      color: var(--text-3);
      font-size: 13px;
    }

    dd {
      margin: 0;
      font-weight: 500;
    }
  `,
})
export class NotificacionesComponent implements OnInit {
  private listaService = inject(ListaEsperaService);
  private solicitudService = inject(SolicitudReventaService);
  private authService = inject(AuthService);
  private toastService = inject(ToastService);

  protected cargando = signal(true);
  protected error = signal('');
  protected enviando = signal(false);
  protected notificaciones = signal<Notificacion[]>([]);
  protected pendientes = signal<SolicitudReventa[]>([]);
  protected respuesta = signal<Respuesta | null>(null);

  protected mensajeConfirmacion = computed(() => {
    const respuesta = this.respuesta();

    if (respuesta == null) {
      return '';
    }

    return respuesta.aceptar
      ? `La boleta de ${respuesta.solicitud.nombreFestival} pasará a tu cuenta y se registrará la reventa. Esto no se puede deshacer.`
      : `La boleta seguirá siendo de ${respuesta.solicitud.nombreVendedor}.`;
  });

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    forkJoin({
      notificaciones: this.listaService.notificaciones(this.authService.sesion()?.documento ?? 0),
      solicitudes: this.solicitudService.recibidas(),
    }).subscribe({
      next: ({ notificaciones, solicitudes }) => {
        this.notificaciones.set(
          [...notificaciones].sort((a, b) => b.fechaHora.localeCompare(a.fechaHora)),
        );
        this.pendientes.set(solicitudes.filter((s) => s.estado === 'PENDIENTE'));
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(mensajeError(err, 'No pudimos cargar tus notificaciones'));
        this.cargando.set(false);
      },
    });
  }

  protected responder(): void {
    const respuesta = this.respuesta();

    this.respuesta.set(null);

    if (respuesta == null || this.enviando()) {
      return;
    }

    const { solicitud, aceptar } = respuesta;
    const peticion = aceptar
      ? this.solicitudService.aceptar(solicitud.idSolicitud)
      : this.solicitudService.rechazar(solicitud.idSolicitud);

    this.enviando.set(true);

    peticion.subscribe({
      next: () => {
        this.enviando.set(false);
        this.toastService.success(
          aceptar ? 'Reventa confirmada' : 'Oferta rechazada',
          aceptar ? 'La boleta ya está en tu cuenta.' : undefined,
        );
        this.cargar();
      },
      error: (err: HttpErrorResponse) => {
        this.enviando.set(false);
        this.toastService.error('No se pudo responder', mensajeError(err, 'Inténtalo de nuevo.'));
        this.cargar();
      },
    });
  }
}
