import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { BoletaDetalle, METODOS_PAGO, estadoFestival } from '../../../models/modelos';
import { AuthService } from '../../../services/auth.service';
import { BoletaService } from '../../../services/boleta.service';
import { ToastService } from '../../../services/toast.service';
import { subirPanel } from '../../../utils/panel';
import { Errores, esDocumento, esVacio, hayErrores, mensajeError } from '../../../utils/validaciones';
import { CampoComponent } from '../../campo/campo';
import { ConfirmDialogComponent } from '../../confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-mis-boletas',
  imports: [CurrencyPipe, DatePipe, FormsModule, RouterLink, CampoComponent, ConfirmDialogComponent],
  templateUrl: './mis-boletas.html',
  styleUrl: './mis-boletas.css',
})
export class MisBoletasComponent implements OnInit {
  private boletaService = inject(BoletaService);
  private authService = inject(AuthService);
  private toastService = inject(ToastService);

  private documento = this.authService.sesion()?.documento ?? 0;

  protected readonly metodos = METODOS_PAGO;

  protected cargando = signal(true);
  protected error = signal('');
  protected pestana = signal<'vigentes' | 'pasadas'>('vigentes');
  private boletas = signal<BoletaDetalle[]>([]);

  protected accion = signal<'revender' | 'devolver' | null>(null);
  protected seleccionada = signal<BoletaDetalle | null>(null);
  protected confirmandoDevolucion = signal(false);
  protected enviando = signal(false);
  protected errorAccion = signal('');
  protected errores = signal<Errores>({});
  protected imprimiendo = signal<number | null>(null);

  protected comprador = '';
  protected precio: number | null = null;
  protected metodo = '';
  protected motivo = '';

  protected vigentes = computed(() =>
    this.boletas()
      .filter((b) => !b.devuelta && estadoFestival(b.festival) !== 'finalizado')
      .sort((a, b) => a.festival.fechaInicio.localeCompare(b.festival.fechaInicio)),
  );

  protected pasadas = computed(() =>
    this.boletas().filter((b) => b.devuelta || estadoFestival(b.festival) === 'finalizado'),
  );

  protected visibles = computed(() =>
    this.pestana() === 'vigentes' ? this.vigentes() : this.pasadas(),
  );

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    this.boletaService.boletasDetalle(this.documento).subscribe({
      next: (boletas) => {
        this.boletas.set(boletas);
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(mensajeError(err, 'No pudimos cargar tus boletas'));
        this.cargando.set(false);
      },
    });
  }

  // El backend solo permite devolver antes de que empiece el festival
  protected puedeDevolver(boleta: BoletaDetalle): boolean {
    return estadoFestival(boleta.festival) === 'proximo';
  }

  protected abrir(boleta: BoletaDetalle, accion: 'revender' | 'devolver'): void {
    this.seleccionada.set(boleta);
    this.accion.set(accion);
    this.errores.set({});
    this.errorAccion.set('');
    this.comprador = '';
    this.precio = accion === 'revender' ? boleta.tipoEntrada.precioBase : null;
    this.metodo = '';
    this.motivo = '';
    subirPanel();
  }

  protected cerrar(): void {
    this.seleccionada.set(null);
    this.accion.set(null);
  }

  protected revender(): void {
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

    this.boletaService
      .revender(boleta.codigoBoleta, Number(this.comprador), Number(this.precio), this.metodo)
      .subscribe({
        next: () => this.terminar('Boleta revendida'),
        error: (err: HttpErrorResponse) => this.fallar(err, 'No se pudo revender la boleta'),
      });
  }

  protected pedirDevolucion(): void {
    const errores: Errores = {};

    if (esVacio(this.motivo)) {
      errores['motivo'] = 'Escribe el motivo de la devolución';
    }

    if (esVacio(this.metodo)) {
      errores['metodo'] = 'Selecciona dónde recibir el reembolso';
    }

    this.errores.set(errores);

    if (!hayErrores(errores)) {
      this.confirmandoDevolucion.set(true);
    }
  }

  protected devolver(): void {
    const boleta = this.seleccionada();

    this.confirmandoDevolucion.set(false);

    if (boleta == null || this.enviando()) {
      return;
    }

    this.enviando.set(true);

    this.boletaService.devolver(boleta.codigoBoleta, this.motivo.trim(), this.metodo).subscribe({
      next: () => this.terminar('Devolución registrada'),
      error: (err: HttpErrorResponse) => this.fallar(err, 'No se pudo registrar la devolución'),
    });
  }

  protected imprimir(codigo: number): void {
    this.imprimiendo.set(codigo);

    // Espera a que la clase de impresión llegue al DOM antes de abrir el diálogo
    setTimeout(() => {
      window.print();
      this.imprimiendo.set(null);
    });
  }

  private terminar(mensaje: string): void {
    this.enviando.set(false);
    this.toastService.success(mensaje);
    this.cerrar();
    this.cargar();
  }

  private fallar(err: HttpErrorResponse, porDefecto: string): void {
    this.enviando.set(false);
    this.errorAccion.set(mensajeError(err, porDefecto));
  }
}
