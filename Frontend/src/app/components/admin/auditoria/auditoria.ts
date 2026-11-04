import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';

import { ControlAcceso, Devolucion, ReventaAutorizada } from '../../../models/modelos';
import { AdminService } from '../../../services/admin.service';
import { mensajeError } from '../../../utils/validaciones';
import { PaginacionComponent, TAMANO_PAGINA } from '../../paginacion/paginacion';

type Pestana = 'accesos' | 'reventas' | 'devoluciones';

// El backend no tiene módulo de fraude ni logs: la auditoría muestra los registros reales
// donde se vería un problema, como ingresos denegados y reventas.
@Component({
  selector: 'app-admin-auditoria',
  imports: [CurrencyPipe, DatePipe, PaginacionComponent],
  templateUrl: './auditoria.html',
})
export class AdminAuditoriaComponent implements OnInit {
  private adminService = inject(AdminService);

  protected cargando = signal(true);
  protected error = signal('');
  protected pestana = signal<Pestana>('accesos');
  protected pagina = signal(1);

  private accesos = signal<ControlAcceso[]>([]);
  private reventas = signal<ReventaAutorizada[]>([]);
  private devoluciones = signal<Devolucion[]>([]);

  protected denegados = computed(() => this.accesos().filter((a) => a.resultado === 'DENEGADO').length);
  protected totalReventas = computed(() => this.reventas().length);
  protected totalDevoluciones = computed(() => this.devoluciones().length);

  protected total = computed(() => {
    const totales: Record<Pestana, number> = {
      accesos: this.accesos().length,
      reventas: this.reventas().length,
      devoluciones: this.devoluciones().length,
    };

    return totales[this.pestana()];
  });

  protected filasAccesos = computed(() => this.enPagina(this.accesos()));
  protected filasReventas = computed(() => this.enPagina(this.reventas()));
  protected filasDevoluciones = computed(() => this.enPagina(this.devoluciones()));

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    forkJoin({
      accesos: this.adminService.controlesAcceso(),
      reventas: this.adminService.reventas(),
      devoluciones: this.adminService.devoluciones(),
    }).subscribe({
      next: ({ accesos, reventas, devoluciones }) => {
        this.accesos.set(
          [...accesos].sort((a, b) => b.fechaHoraIngreso.localeCompare(a.fechaHoraIngreso)),
        );
        this.reventas.set([...reventas].sort((a, b) => b.fechaHora.localeCompare(a.fechaHora)));
        this.devoluciones.set(
          [...devoluciones].sort((a, b) => b.fechaHora.localeCompare(a.fechaHora)),
        );
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(mensajeError(err, 'No pudimos cargar la auditoría'));
        this.cargando.set(false);
      },
    });
  }

  protected cambiarPestana(pestana: Pestana): void {
    this.pestana.set(pestana);
    this.pagina.set(1);
  }

  private enPagina<T>(lista: T[]): T[] {
    return lista.slice((this.pagina() - 1) * TAMANO_PAGINA, this.pagina() * TAMANO_PAGINA);
  }
}
