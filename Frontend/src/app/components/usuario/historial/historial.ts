import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';

import { ReventaAutorizada, Transaccion } from '../../../models/modelos';
import { AuthService } from '../../../services/auth.service';
import { BoletaService } from '../../../services/boleta.service';
import { mensajeError } from '../../../utils/validaciones';
import { PaginacionComponent, TAMANO_PAGINA } from '../../paginacion/paginacion';

const ETIQUETA_TIPO: Record<string, string> = {
  VENTA_PRIMARIA: 'Compra',
  REVENTA_AUTORIZADA: 'Reventa',
  DEVOLUCION: 'Devolución',
};

@Component({
  selector: 'app-historial',
  imports: [CurrencyPipe, DatePipe, PaginacionComponent],
  templateUrl: './historial.html',
})
export class HistorialComponent implements OnInit {
  private boletaService = inject(BoletaService);
  private authService = inject(AuthService);

  private documento = this.authService.sesion()?.documento ?? 0;

  protected cargando = signal(true);
  protected error = signal('');
  protected pagina = signal(1);
  private transacciones = signal<Transaccion[]>([]);

  protected filas = computed(() =>
    this.transacciones().slice((this.pagina() - 1) * TAMANO_PAGINA, this.pagina() * TAMANO_PAGINA),
  );

  protected total = computed(() => this.transacciones().length);

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    // El backend no tiene un historial por usuario: se arma desde sus boletas y sus reventas
    forkJoin({
      boletas: this.boletaService.boletasDetalle(this.documento),
      vendidas: this.boletaService.reventasComoVendedor(this.documento),
      compradas: this.boletaService.reventasComoComprador(this.documento),
    }).subscribe({
      next: ({ boletas, vendidas, compradas }) => {
        this.transacciones.set(
          HistorialComponent.unir([
            ...boletas.flatMap((b) => b.transacciones),
            ...vendidas,
            ...compradas,
          ]),
        );
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(mensajeError(err, 'No pudimos cargar tu historial'));
        this.cargando.set(false);
      },
    });
  }

  protected etiqueta(transaccion: Transaccion): string {
    const reventa = transaccion as ReventaAutorizada;

    if (reventa.vendedorDocumento === this.documento) {
      return 'Reventa (vendida)';
    }

    if (reventa.compradorDocumento === this.documento) {
      return 'Reventa (comprada)';
    }

    return ETIQUETA_TIPO[transaccion.tipoTransaccion] ?? transaccion.tipoTransaccion;
  }

  // Una reventa puede llegar por la boleta y por la lista de reventas: gana la más completa
  private static unir(transacciones: Transaccion[]): Transaccion[] {
    const unicas = new Map<number, Transaccion>();

    transacciones.forEach((t) =>
      unicas.set(t.idTransaccion, { ...unicas.get(t.idTransaccion), ...t }),
    );

    return [...unicas.values()].sort((a, b) => b.fechaHora.localeCompare(a.fechaHora));
  }
}
