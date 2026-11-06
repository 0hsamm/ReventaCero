import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { BoletaDetalle, PublicacionReventa, estadoFestival } from '../../../models/modelos';
import { AuthService } from '../../../services/auth.service';
import { BoletaService } from '../../../services/boleta.service';
import { PublicacionReventaService } from '../../../services/publicacion-reventa.service';
import { ToastService } from '../../../services/toast.service';
import { subirPanel } from '../../../utils/panel';
import { Errores, hayErrores, mensajeError } from '../../../utils/validaciones';
import { CampoComponent } from '../../campo/campo';
import { ConfirmDialogComponent } from '../../confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-revender',
  imports: [CurrencyPipe, DatePipe, FormsModule, RouterLink, CampoComponent, ConfirmDialogComponent],
  templateUrl: './revender.html',
  styles: '.aviso-tope { margin-bottom: 16px; }',
})
export class RevenderComponent implements OnInit {
  private boletaService = inject(BoletaService);
  private publicacionService = inject(PublicacionReventaService);
  private authService = inject(AuthService);
  private toastService = inject(ToastService);

  private documento = this.authService.sesion()?.documento ?? 0;

  // Mis boletas llega aquí con la boleta ya elegida; solo aplica a la primera carga
  private boletaPedida: number | null =
    Number(inject(ActivatedRoute).snapshot.queryParamMap.get('boleta')) || null;

  protected cargando = signal(true);
  protected error = signal('');
  protected enviando = signal(false);
  protected errorAccion = signal('');
  protected errores = signal<Errores>({});
  protected seleccionada = signal<BoletaDetalle | null>(null);
  protected retirando = signal<BoletaDetalle | null>(null);
  private boletas = signal<BoletaDetalle[]>([]);
  private publicaciones = signal<PublicacionReventa[]>([]);

  protected precio: number | null = null;

  protected disponibles = computed(() =>
    this.boletas()
      .filter((b) => !b.devuelta && estadoFestival(b.festival) !== 'finalizado')
      .sort((a, b) => a.festival.fechaInicio.localeCompare(b.festival.fechaInicio)),
  );

  private publicadas = computed(
    () => new Map(this.publicaciones().map((p) => [p.codigoBoleta, p])),
  );

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    forkJoin({
      boletas: this.boletaService.boletasDetalle(this.documento),
      publicaciones: this.publicacionService.mias(),
    }).subscribe({
      next: ({ boletas, publicaciones }) => {
        this.boletas.set(boletas);
        this.publicaciones.set(publicaciones);
        this.cargando.set(false);

        const pedida = this.disponibles().find((b) => b.codigoBoleta === this.boletaPedida);

        this.boletaPedida = null;

        if (pedida && !this.publicacionDe(pedida)) {
          this.abrir(pedida);
        }
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(mensajeError(err, 'No pudimos cargar tus boletas'));
        this.cargando.set(false);
      },
    });
  }

  protected publicacionDe(boleta: BoletaDetalle): PublicacionReventa | undefined {
    return this.publicadas().get(boleta.codigoBoleta);
  }

  protected abrir(boleta: BoletaDetalle): void {
    this.seleccionada.set(boleta);
    this.errores.set({});
    this.errorAccion.set('');
    this.precio = boleta.tipoEntrada.precioBase;
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

    const errores: Errores = {};

    if (this.precio == null || this.precio < 0) {
      errores['precio'] = 'Escribe el precio de venta';
    } else if (this.precio > boleta.tipoEntrada.precioTopeReventa) {
      errores['precio'] = 'El precio no puede superar el tope de reventa';
    }

    this.errores.set(errores);

    if (hayErrores(errores) || this.enviando()) {
      return;
    }

    this.enviando.set(true);

    this.publicacionService.publicar(boleta.codigoBoleta, Number(this.precio)).subscribe({
      next: () => {
        this.enviando.set(false);
        this.toastService.success(
          'Boleta publicada',
          'La boleta sigue siendo tuya hasta que alguien la compre.',
        );
        this.cerrar();
        this.cargar();
      },
      error: (err: HttpErrorResponse) => {
        this.enviando.set(false);
        this.errorAccion.set(mensajeError(err, 'No se pudo publicar la boleta'));
      },
    });
  }

  protected confirmarRetiro(): void {
    const boleta = this.retirando();

    this.retirando.set(null);

    if (boleta == null) {
      return;
    }

    this.publicacionService.retirar(boleta.codigoBoleta).subscribe({
      next: () => {
        this.toastService.success('Publicación retirada');
        this.cargar();
      },
      error: (err: HttpErrorResponse) => {
        this.toastService.error('No se pudo retirar', mensajeError(err, 'Inténtalo de nuevo.'));
        this.cargar();
      },
    });
  }
}
