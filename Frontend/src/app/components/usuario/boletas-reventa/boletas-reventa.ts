import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { METODOS_PAGO, PublicacionReventa } from '../../../models/modelos';
import { PublicacionReventaService } from '../../../services/publicacion-reventa.service';
import { ToastService } from '../../../services/toast.service';
import { subirPanel } from '../../../utils/panel';
import { Errores, esVacio, hayErrores, mensajeError } from '../../../utils/validaciones';
import { CampoComponent } from '../../campo/campo';
import { PaginacionComponent, TAMANO_PAGINA } from '../../paginacion/paginacion';

@Component({
  selector: 'app-boletas-reventa',
  imports: [CurrencyPipe, DatePipe, FormsModule, RouterLink, CampoComponent, PaginacionComponent],
  templateUrl: './boletas-reventa.html',
  styles: '.aviso-compra { margin-bottom: 16px; }',
})
export class BoletasReventaComponent implements OnInit {
  private publicacionService = inject(PublicacionReventaService);
  private toastService = inject(ToastService);

  protected readonly metodos = METODOS_PAGO;

  protected cargando = signal(true);
  protected error = signal('');
  protected enviando = signal(false);
  protected errorCompra = signal('');
  protected errores = signal<Errores>({});
  protected seleccionada = signal<PublicacionReventa | null>(null);
  protected pagina = signal(1);
  private publicaciones = signal<PublicacionReventa[]>([]);

  protected metodo = '';

  protected total = computed(() => this.publicaciones().length);
  protected filas = computed(() =>
    this.publicaciones().slice((this.pagina() - 1) * TAMANO_PAGINA, this.pagina() * TAMANO_PAGINA),
  );

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    this.publicacionService.disponibles().subscribe({
      next: (lista) => {
        this.publicaciones.set(lista);
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(mensajeError(err, 'No pudimos cargar las boletas en reventa'));
        this.cargando.set(false);
      },
    });
  }

  protected abrir(publicacion: PublicacionReventa): void {
    this.seleccionada.set(publicacion);
    this.metodo = '';
    this.errores.set({});
    this.errorCompra.set('');
    subirPanel();
  }

  protected cerrar(): void {
    this.seleccionada.set(null);
  }

  protected comprar(): void {
    const publicacion = this.seleccionada();
    const errores: Errores = {};

    if (publicacion == null) {
      return;
    }

    if (esVacio(this.metodo)) {
      errores['metodo'] = 'Selecciona un método de pago';
    }

    this.errores.set(errores);

    if (hayErrores(errores) || this.enviando()) {
      return;
    }

    this.enviando.set(true);

    this.publicacionService.comprar(publicacion.idPublicacion, this.metodo).subscribe({
      next: () => {
        this.enviando.set(false);
        this.toastService.success('Boleta comprada', 'Ya está en Mis boletas.');
        this.cerrar();
        this.cargar();
      },
      error: (err: HttpErrorResponse) => {
        this.enviando.set(false);
        this.errorCompra.set(mensajeError(err, 'No se pudo comprar la boleta'));
        // Puede que otra persona la haya comprado primero: la lista debe reflejarlo
        this.cargar();
      },
    });
  }
}
