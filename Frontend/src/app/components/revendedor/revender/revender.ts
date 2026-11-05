import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import {
  BoletaDetalle,
  CLASE_ESTADO_PUBLICACION,
  CLASE_ESTADO_SOLICITUD,
  ETIQUETA_ESTADO_PUBLICACION,
  ETIQUETA_ESTADO_SOLICITUD,
  METODOS_PAGO,
  PublicacionReventa,
  SolicitudReventa,
  estadoFestival,
} from '../../../models/modelos';
import { AuthService } from '../../../services/auth.service';
import { BoletaService } from '../../../services/boleta.service';
import { PublicacionReventaService } from '../../../services/publicacion-reventa.service';
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

type ModoReventa = 'publicar' | 'ofrecer';

@Component({
  selector: 'app-revender',
  imports: [CurrencyPipe, DatePipe, FormsModule, RouterLink, CampoComponent, ConfirmDialogComponent],
  templateUrl: './revender.html',
  styles: '.aviso-tope { margin-bottom: 16px; } .subtitulo { margin-top: 32px; margin-bottom: 12px; }',
})
export class RevenderComponent implements OnInit {
  private boletaService = inject(BoletaService);
  private solicitudService = inject(SolicitudReventaService);
  private publicacionService = inject(PublicacionReventaService);
  private authService = inject(AuthService);
  private toastService = inject(ToastService);

  private documento = this.authService.sesion()?.documento ?? 0;

  // Mis boletas llega aquí con la boleta ya elegida; solo aplica a la primera carga
  private boletaPedida: number | null =
    Number(inject(ActivatedRoute).snapshot.queryParamMap.get('boleta')) || null;

  protected readonly metodos = METODOS_PAGO;
  protected readonly etiquetaEstado = ETIQUETA_ESTADO_SOLICITUD;
  protected readonly claseEstado = CLASE_ESTADO_SOLICITUD;
  protected readonly etiquetaPublicacion = ETIQUETA_ESTADO_PUBLICACION;
  protected readonly clasePublicacion = CLASE_ESTADO_PUBLICACION;

  protected cargando = signal(true);
  protected error = signal('');
  protected enviando = signal(false);
  protected errorAccion = signal('');
  protected errores = signal<Errores>({});
  protected seleccionada = signal<BoletaDetalle | null>(null);
  protected modo = signal<ModoReventa>('publicar');
  protected cancelando = signal<SolicitudReventa | null>(null);
  protected retirando = signal<PublicacionReventa | null>(null);
  protected enviadas = signal<SolicitudReventa[]>([]);
  protected publicaciones = signal<PublicacionReventa[]>([]);
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

  private publicadas = computed(
    () =>
      new Map(
        this.publicaciones()
          .filter((p) => p.estado === 'DISPONIBLE')
          .map((p) => [p.codigoBoleta, p]),
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
      publicaciones: this.publicacionService.mias(),
    }).subscribe({
      next: ({ boletas, enviadas, publicaciones }) => {
        this.boletas.set(boletas);
        this.enviadas.set(enviadas);
        this.publicaciones.set(publicaciones);
        this.cargando.set(false);

        const pedida = this.disponibles().find((b) => b.codigoBoleta === this.boletaPedida);

        this.boletaPedida = null;

        if (pedida && this.estaLibre(pedida)) {
          this.abrir(pedida, 'publicar');
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

  protected publicacionDe(boleta: BoletaDetalle): PublicacionReventa | undefined {
    return this.publicadas().get(boleta.codigoBoleta);
  }

  protected abrir(boleta: BoletaDetalle, modo: ModoReventa): void {
    this.seleccionada.set(boleta);
    this.modo.set(modo);
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

  protected publicar(): void {
    const boleta = this.seleccionada();

    if (boleta == null) {
      return;
    }

    const errores = this.validarPrecio(boleta);

    this.errores.set(errores);

    if (hayErrores(errores) || this.enviando()) {
      return;
    }

    this.enviando.set(true);

    this.publicacionService.publicar(boleta.codigoBoleta, Number(this.precio)).subscribe({
      next: () =>
        this.terminar('Boleta publicada', 'La boleta sigue siendo tuya hasta que alguien la compre.'),
      error: (err: HttpErrorResponse) => this.fallar(err, 'No se pudo publicar la boleta'),
    });
  }

  protected ofrecer(): void {
    const boleta = this.seleccionada();

    if (boleta == null) {
      return;
    }

    const errores = this.validarPrecio(boleta);

    if (!esDocumento(this.comprador)) {
      errores['comprador'] = 'Escribe el documento del comprador: de 5 a 15 dígitos';
    } else if (Number(this.comprador) === this.documento) {
      errores['comprador'] = 'No puedes revenderte la boleta a ti mismo';
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
        next: () =>
          this.terminar('Oferta enviada', 'La boleta sigue siendo tuya hasta que la acepten.'),
        error: (err: HttpErrorResponse) => this.fallar(err, 'No se pudo enviar la oferta'),
      });
  }

  protected confirmarCancelacion(): void {
    const solicitud = this.cancelando();

    this.cancelando.set(null);

    if (solicitud != null) {
      this.solicitudService.cancelar(solicitud.idSolicitud).subscribe({
        next: () => this.despuesDeCerrar('Oferta cancelada'),
        error: (err: HttpErrorResponse) => this.errorAlCerrar(err, 'No se pudo cancelar'),
      });
    }
  }

  protected confirmarRetiro(): void {
    const publicacion = this.retirando();

    this.retirando.set(null);

    if (publicacion != null) {
      this.publicacionService.retirar(publicacion.idPublicacion).subscribe({
        next: () => this.despuesDeCerrar('Publicación retirada'),
        error: (err: HttpErrorResponse) => this.errorAlCerrar(err, 'No se pudo retirar'),
      });
    }
  }

  private estaLibre(boleta: BoletaDetalle): boolean {
    return !this.tieneOfertaPendiente(boleta) && !this.publicacionDe(boleta);
  }

  private validarPrecio(boleta: BoletaDetalle): Errores {
    if (this.precio == null || this.precio < 0) {
      return { precio: 'Escribe el precio de venta' };
    }

    return this.precio > boleta.tipoEntrada.precioTopeReventa
      ? { precio: 'El precio no puede superar el tope de reventa' }
      : {};
  }

  private terminar(titulo: string, detalle: string): void {
    this.enviando.set(false);
    this.toastService.success(titulo, detalle);
    this.cerrar();
    this.cargar();
  }

  private fallar(err: HttpErrorResponse, porDefecto: string): void {
    this.enviando.set(false);
    this.errorAccion.set(mensajeError(err, porDefecto));
  }

  private despuesDeCerrar(mensaje: string): void {
    this.toastService.success(mensaje);
    this.cargar();
  }

  private errorAlCerrar(err: HttpErrorResponse, titulo: string): void {
    this.toastService.error(titulo, mensajeError(err, 'Inténtalo de nuevo.'));
    this.cargar();
  }
}
