import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';

import { ReventaAutorizada } from '../../../models/modelos';
import { AuthService } from '../../../services/auth.service';
import { BoletaService } from '../../../services/boleta.service';
import { mensajeError } from '../../../utils/validaciones';
import { PaginacionComponent, TAMANO_PAGINA } from '../../paginacion/paginacion';

@Component({
  selector: 'app-ventas-revendedor',
  imports: [CurrencyPipe, DatePipe, PaginacionComponent],
  templateUrl: './ventas.html',
})
export class VentasRevendedorComponent implements OnInit {
  private boletaService = inject(BoletaService);
  private authService = inject(AuthService);

  private documento = this.authService.sesion()?.documento ?? 0;

  protected cargando = signal(true);
  protected error = signal('');
  protected pagina = signal(1);
  private ventas = signal<ReventaAutorizada[]>([]);

  protected total = computed(() => this.ventas().length);
  protected totalVendido = computed(() => this.ventas().reduce((suma, v) => suma + v.precio, 0));
  protected totalComision = computed(() =>
    this.ventas().reduce((suma, v) => suma + v.comisionOrganizador, 0),
  );
  protected filas = computed(() =>
    this.ventas().slice((this.pagina() - 1) * TAMANO_PAGINA, this.pagina() * TAMANO_PAGINA),
  );

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    this.boletaService.reventasComoVendedor(this.documento).subscribe({
      next: (ventas) => {
        this.ventas.set([...ventas].sort((a, b) => b.fechaHora.localeCompare(a.fechaHora)));
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(mensajeError(err, 'No pudimos cargar tus ventas'));
        this.cargando.set(false);
      },
    });
  }
}
