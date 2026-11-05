import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import {
  BoletaDetalle,
  CLASE_ESTADO_SOLICITUD,
  ETIQUETA_ESTADO_SOLICITUD,
  METODOS_PAGO,
  SolicitudReventa,
  estadoFestival,
} from '../../../models/modelos';
import { AuthService } from '../../../services/auth.service';
import { BoletaService } from '../../../services/boleta.service';
import { SolicitudReventaService } from '../../../services/solicitud-reventa.service';
import { ToastService } from '../../../services/toast.service';
import { subirPanel } from '../../../utils/panel';
import {
  Errores,
  esDocumento,
  esVacio,
  hayErrores,
  mensajeError,
} from '../../../utils/validaciones';
import { CampoComponent } from '../../campo/campo';
import { ConfirmDialogComponent } from '../../confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-revender',
  imports: [CurrencyPipe, DatePipe, FormsModule, RouterLink, CampoComponent, ConfirmDialogComponent],
  templateUrl: './revender.html',
  styles: '.aviso-tope { margin-bottom: 16px; } .enviadas { margin-top: 32px; margin-bottom: 12px; }',
})
export class RevenderComponent implements OnInit {
  private boletaService = inject(BoletaService);
  private solicitudService = inject(SolicitudReventaService);
  private authService = inject(AuthService);
  private toastService = inject(ToastService);

  private documento = this.authService.sesion()?.documento ?? 0;

  // Mis boletas llega aquí con la boleta ya elegida; solo aplica a la primera carga
  private boletaPedida: number | null =
    Number(inject(ActivatedRoute).snapshot.queryParamMap.get('boleta')) || null;

  protected readonly metodos = METODOS_PAGO;
  protected readonly etiquetaEstado = ETIQUETA_ESTADO_SOLICITUD;
  protected readonly claseEstado = CLASE_ESTADO_SOLICITUD;

  protected cargando = signal(true);
  protected error = signal('');
  protected enviando = signal(false);
  protected errorAccion = signal('');
  protected errores = signal<Errores>({});
  protected seleccionada = signal<BoletaDetalle | null>(null);
  protected cancelando = signal<SolicitudReventa | null>(null);
  protected enviadas = signal<SolicitudReventa[]>([]);
  private boletas = signal<BoletaDetalle[]>([]);

  protected comprador = '';
  protected precio: number | null = null;
  protected metodo = '';

  protected disponibles = computed(() =>
    this.boletas()
      .filter((b) => !b.devuelta && estadoFestival(b.festival) !== 'finalizado')
      .sort((a, b) => a.festival.fechaInicio.localeCompare(b.festival.fechaInicio)),
  );

  private enEspera = computed(
    () =>
      new Set(
        this.enviadas()
          .filter((s) => s.estado === 'PENDIENTE')
          .map((s) => s.codigoBoleta),
      ),
  );

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    forkJoin({
      boletas: this.boletaService.boletasDetalle(this.documento),
      enviadas: this.solicitudService.enviadas(),
    }).subscribe({
      next: ({ boletas, enviadas }) => {
        this.boletas.set(boletas);
        this.enviadas.set(enviadas);
        this.cargando.set(false);

        const pedida = this.disponibles().find((b) => b.codigoBoleta === this.boletaPedida);

        this.boletaPedida = null;

        if (pedida && !this.tieneOfertaPendiente(pedida)) {
          this.abrir(pedida);
        }
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(mensajeError(err, 'No pudimos cargar tus boletas'));
        this.cargando.set(false);
      },
    });
  }

  protected tieneOfertaPendiente(boleta: BoletaDetalle): boolean {
    return this.enEspera().has(boleta.codigoBoleta);
  }

  protected abrir(boleta: BoletaDetalle): void {
    this.seleccionada.set(boleta);
    this.errores.set({});
    this.errorAccion.set('');
    this.comprador = '';
    this.precio = boleta.tipoEntrada.precioBase;
    this.metodo = '';
    subirPanel();
  }

  protected cerrar(): void {
    this.seleccionada.set(null);
  }

  protected ofrecer(): void {
    const boleta = this.seleccionada();
    const errores: Errores = {};

    if (boleta == null) {
      return;
    }

    if (!esDocumento(this.comprador)) {
      errores['comprador'] = 'Escribe el documento del comprador: de 5 a 15 dígitos';
    } else if (Number(this.comprador) === this.documento) {
      errores['comprador'] = 'No puedes revenderte la boleta a ti mismo';
    }

    if (this.precio == null || this.precio < 0) {
      errores['precio'] = 'Escribe el precio de venta';
    } else if (this.precio > boleta.tipoEntrada.precioTopeReventa) {
      errores['precio'] = 'El precio no puede superar el tope de reventa';
    }

    if (esVacio(this.metodo)) {
      errores['metodo'] = 'Selecciona un método de pago';
    }

    this.errores.set(errores);

    if (hayErrores(errores) || this.enviando()) {
      return;
    }

    this.enviando.set(true);

    this.solicitudService
      .ofrecer(boleta.codigoBoleta, Number(this.comprador), Number(this.precio), this.metodo)
      .subscribe({
        next: () => {
          this.enviando.set(false);
          this.toastService.success('Oferta enviada', 'La boleta sigue siendo tuya hasta que la acepten.');
          this.cerrar();
          this.cargar();
        },
        error: (err: HttpErrorResponse) => {
          this.enviando.set(false);
          this.errorAccion.set(mensajeError(err, 'No se pudo enviar la oferta'));
        },
      });
  }

  protected confirmarCancelacion(): void {
    const solicitud = this.cancelando();

    this.cancelando.set(null);

    if (solicitud == null) {
      return;
    }

    this.solicitudService.cancelar(solicitud.idSolicitud).subscribe({
      next: () => {
        this.toastService.success('Oferta cancelada');
        this.cargar();
      },
      error: (err: HttpErrorResponse) => {
        this.toastService.error('No se pudo cancelar', mensajeError(err, 'Inténtalo de nuevo.'));
        this.cargar();
      },
    });
  }
}
