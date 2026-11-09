import { DecimalPipe, formatCurrency, formatDate } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, LOCALE_ID, OnInit, computed, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';

import {
  Boleta,
  Festival,
  TipoEntrada,
  Transaccion,
  Usuario,
  Zona,
  estadoFestival,
} from '../../../models/modelos';
import { AdminService } from '../../../services/admin.service';
import { FestivalService } from '../../../services/festival.service';
import { mensajeError } from '../../../utils/validaciones';
import { DatoGrafica, GraficaBarrasComponent } from '../../grafica-barras/grafica-barras';

@Component({
  selector: 'app-admin-dashboard',
  imports: [DecimalPipe, GraficaBarrasComponent],
  templateUrl: './dashboard.html',
})
export class AdminDashboardComponent implements OnInit {
  private adminService = inject(AdminService);
  private festivalService = inject(FestivalService);
  private locale = inject(LOCALE_ID);

  protected cargando = signal(true);
  protected error = signal('');

  private usuarios = signal<Usuario[]>([]);
  private festivales = signal<Festival[]>([]);
  private boletas = signal<Boleta[]>([]);
  private transacciones = signal<Transaccion[]>([]);
  private tipos = signal<TipoEntrada[]>([]);
  private zonas = signal<Zona[]>([]);

  protected totalBoletas = computed(() => this.boletas().length);
  protected usuariosActivos = computed(
    () => this.usuarios().filter((usuario) => usuario.estadoCuenta === 'ACTIVA').length,
  );
  protected festivalesVigentes = computed(
    () => this.festivales().filter((f) => estadoFestival(f) !== 'finalizado').length,
  );

  // Las devoluciones no suman: son dinero que sale
  private ventas = computed(() =>
    this.transacciones().filter((t) => t.tipoTransaccion !== 'DEVOLUCION'),
  );

  protected volumen = computed(() => this.ventas().reduce((suma, t) => suma + t.precio, 0));

  protected ventasPorMes = computed<DatoGrafica[]>(() => {
    const meses = new Map<string, number>();

    this.ventas().forEach((t) => {
      const mes = t.fechaHora.slice(0, 7);

      meses.set(mes, (meses.get(mes) ?? 0) + t.precio);
    });

    return [...meses.entries()]
      .sort((a, b) => a[0].localeCompare(b[0]))
      .map(([mes, valor]) => ({
        etiqueta: formatDate(`${mes}-01`, 'MMM y', this.locale),
        valor,
        texto: formatCurrency(valor, this.locale, '$', 'COP', '1.0-0'),
      }));
  });

  protected boletasPorFestival = computed<DatoGrafica[]>(() => {
    const conteo = new Map<number, number>();

    this.boletas().forEach((boleta) => {
      const tipo = this.tipos().find((t) => t.idTipoEntrada === boleta.idTipoEntrada);
      const zona = this.zonas().find((z) => z.idZona === tipo?.idZona);

      if (zona) {
        conteo.set(zona.idFestival, (conteo.get(zona.idFestival) ?? 0) + 1);
      }
    });

    return [...conteo.entries()]
      .sort((a, b) => b[1] - a[1])
      .map(([id, valor]) => ({
        etiqueta: this.festivales().find((f) => f.idFestival === id)?.nombre ?? `Festival ${id}`,
        valor,
        texto: `${valor} boleta(s)`,
      }));
  });

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    forkJoin({
      usuarios: this.adminService.usuarios(),
      festivales: this.festivalService.listar(),
      boletas: this.adminService.boletas(),
      transacciones: this.adminService.transacciones(),
      tipos: this.festivalService.listarTipos(),
      zonas: this.festivalService.listarZonas(),
    }).subscribe({
      next: ({ usuarios, festivales, boletas, transacciones, tipos, zonas }) => {
        this.usuarios.set(usuarios);
        this.festivales.set(festivales);
        this.boletas.set(boletas);
        this.transacciones.set(transacciones);
        this.tipos.set(tipos);
        this.zonas.set(zonas);
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(mensajeError(err, 'No pudimos cargar las estadísticas'));
        this.cargando.set(false);
      },
    });
  }
}
