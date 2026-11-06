import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Observable, forkJoin, of, switchMap, tap } from 'rxjs';

import { Patrocinador, Patrocinio } from '../../../models/modelos';
import { PatrocinioService } from '../../../services/patrocinio.service';
import { ToastService } from '../../../services/toast.service';
import { Errores, esVacio, hayErrores, mensajeError } from '../../../utils/validaciones';
import { CampoComponent } from '../../campo/campo';
import { ConfirmDialogComponent } from '../../confirm-dialog/confirm-dialog';

const SIN_ELEGIR = 0;
const NUEVO = -1;

// Patrocinios de un festival ya creado: el backend los exige ligados a un festival existente.
@Component({
  selector: 'app-patrocinios-festival',
  imports: [CurrencyPipe, FormsModule, CampoComponent, ConfirmDialogComponent],
  templateUrl: './patrocinios-festival.html',
})
export class PatrociniosFestivalComponent implements OnInit {
  private patrocinioService = inject(PatrocinioService);
  private toastService = inject(ToastService);
  private cambios = inject(ChangeDetectorRef);

  readonly idFestival = input.required<number>();
  readonly titulo = input('Patrocinios');

  protected readonly NUEVO = NUEVO;
  protected readonly SIN_ELEGIR = SIN_ELEGIR;

  protected cargando = signal(true);
  protected error = signal('');
  protected enviando = signal(false);
  protected errorForm = signal('');
  protected errores = signal<Errores>({});
  protected patrocinios = signal<Patrocinio[]>([]);
  protected patrocinadores = signal<Patrocinador[]>([]);
  protected total = signal(0);
  protected eliminando = signal<Patrocinio | null>(null);

  protected form = PatrociniosFestivalComponent.formVacio();

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.error.set('');

    forkJoin({
      patrocinios: this.patrocinioService.patrociniosDeFestival(this.idFestival()),
      patrocinadores: this.patrocinioService.listarPatrocinadores(),
      total: this.patrocinioService.totalDeFestival(this.idFestival()),
    }).subscribe({
      next: ({ patrocinios, patrocinadores, total }) => {
        this.patrocinios.set(patrocinios);
        this.patrocinadores.set(patrocinadores);
        this.total.set(total);

        // Sin patrocinadores en el catálogo, lo único posible es crear uno
        if (!patrocinadores.length && this.form.idPatrocinador === SIN_ELEGIR) {
          this.form.idPatrocinador = NUEVO;
        }

        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(mensajeError(err, 'No pudimos cargar los patrocinios'));
        this.cargando.set(false);
      },
    });
  }

  protected nombrePatrocinador(id: number): string {
    return this.patrocinadores().find((p) => p.idPatrocinador === id)?.nombre ?? 'Patrocinador eliminado';
  }

  protected editar(patrocinio: Patrocinio): void {
    this.form = {
      ...PatrociniosFestivalComponent.formVacio(),
      idPatrocinio: patrocinio.idPatrocinio,
      idPatrocinador: patrocinio.idPatrocinador,
      tipoAporte: patrocinio.tipoAporte,
      monto: patrocinio.monto,
    };
    this.errores.set({});
    this.errorForm.set('');
  }

  protected cancelarEdicion(): void {
    this.limpiar();
  }

  protected guardar(): void {
    const errores = this.validar();

    this.errores.set(errores);
    this.errorForm.set('');

    if (hayErrores(errores) || this.enviando()) {
      return;
    }

    this.enviando.set(true);

    this.resolverPatrocinador()
      .pipe(
        switchMap((idPatrocinador) => {
          const patrocinio = {
            idFestival: this.idFestival(),
            idPatrocinador,
            tipoAporte: this.form.tipoAporte.trim(),
            monto: Number(this.form.monto),
          };

          return this.form.idPatrocinio
            ? this.patrocinioService.actualizarPatrocinio(this.form.idPatrocinio, patrocinio)
            : this.patrocinioService.crearPatrocinio(patrocinio);
        }),
      )
      .subscribe({
        next: () => {
          this.enviando.set(false);
          this.toastService.success('Patrocinio guardado');
          this.limpiar();
          this.cargar();
        },
        error: (err: HttpErrorResponse) => {
          this.enviando.set(false);
          this.errorForm.set(mensajeError(err, 'No se pudo guardar el patrocinio'));
          this.cambios.markForCheck();
        },
      });
  }

  protected confirmarEliminacion(): void {
    const patrocinio = this.eliminando();

    this.eliminando.set(null);

    if (patrocinio == null) {
      return;
    }

    this.patrocinioService.eliminarPatrocinio(patrocinio.idPatrocinio).subscribe({
      next: () => {
        this.toastService.success('Patrocinio eliminado');

        if (this.form.idPatrocinio === patrocinio.idPatrocinio) {
          this.limpiar();
        }

        this.cargar();
      },
      error: (err: HttpErrorResponse) =>
        this.toastService.error('No se pudo eliminar', mensajeError(err, 'Inténtalo de nuevo.')),
    });
  }

  // Si el patrocinio falla después de crear el patrocinador, este ya existe: al reintentar se usa
  // ese mismo en vez de chocar con la regla de nombre único.
  private resolverPatrocinador(): Observable<number> {
    if (this.form.idPatrocinador !== NUEVO) {
      return of(this.form.idPatrocinador);
    }

    return this.patrocinioService
      .crearPatrocinador({
        nombre: this.form.nombre.trim(),
        tipo: this.form.tipo.trim(),
        contacto: this.form.contacto.trim(),
      })
      .pipe(
        tap((creado) => {
          this.patrocinadores.update((lista) => [...lista, creado]);
          this.form.idPatrocinador = creado.idPatrocinador;
        }),
        switchMap((creado) => of(creado.idPatrocinador)),
      );
  }

  private validar(): Errores {
    const errores: Errores = {};
    const { idPatrocinador, nombre, tipoAporte, monto } = this.form;

    if (idPatrocinador === SIN_ELEGIR) {
      errores['patrocinador'] = 'Selecciona el patrocinador';
    }

    if (idPatrocinador === NUEVO && esVacio(nombre)) {
      errores['nombre'] = 'Escribe el nombre del patrocinador';
    }

    if (esVacio(tipoAporte)) {
      errores['tipoAporte'] = 'Escribe el tipo de aporte';
    }

    if (monto == null || monto <= 0) {
      errores['monto'] = 'Escribe un monto mayor que cero';
    }

    return errores;
  }

  private limpiar(): void {
    this.form = PatrociniosFestivalComponent.formVacio();

    if (!this.patrocinadores().length) {
      this.form.idPatrocinador = NUEVO;
    }

    this.errores.set({});
    this.errorForm.set('');
    this.cambios.markForCheck();
  }

  private static formVacio() {
    return {
      idPatrocinio: 0,
      idPatrocinador: SIN_ELEGIR,
      nombre: '',
      tipo: '',
      contacto: '',
      tipoAporte: '',
      monto: null as number | null,
    };
  }
}
