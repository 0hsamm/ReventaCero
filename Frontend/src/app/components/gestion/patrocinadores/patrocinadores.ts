import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Patrocinador } from '../../../models/modelos';
import { PatrocinioService } from '../../../services/patrocinio.service';
import { ToastService } from '../../../services/toast.service';
import { Errores, esVacio, hayErrores, mensajeError } from '../../../utils/validaciones';
import { CampoComponent } from '../../campo/campo';
import { ConfirmDialogComponent } from '../../confirm-dialog/confirm-dialog';
import { PaginacionComponent, TAMANO_PAGINA } from '../../paginacion/paginacion';

@Component({
  selector: 'app-gestion-patrocinadores',
  imports: [FormsModule, CampoComponent, ConfirmDialogComponent, PaginacionComponent],
  templateUrl: './patrocinadores.html',
})
export class GestionPatrocinadoresComponent implements OnInit {
  private patrocinioService = inject(PatrocinioService);
  private toastService = inject(ToastService);
  private cambios = inject(ChangeDetectorRef);

  protected cargando = signal(true);
  protected error = signal('');
  protected enviando = signal(false);
  protected errorForm = signal('');
  protected errores = signal<Errores>({});
  protected editando = signal<Patrocinador | null>(null);
  protected eliminando = signal<Patrocinador | null>(null);
  protected pagina = signal(1);
  private patrocinadores = signal<Patrocinador[]>([]);

  protected nombre = '';
  protected tipo = '';
  protected contacto = '';

  protected total = computed(() => this.patrocinadores().length);
  protected filas = computed(() =>
    this.patrocinadores().slice((this.pagina() - 1) * TAMANO_PAGINA, this.pagina() * TAMANO_PAGINA),
  );

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.error.set('');

    this.patrocinioService.listarPatrocinadores().subscribe({
      next: (lista) => {
        this.patrocinadores.set([...lista].sort((a, b) => a.nombre.localeCompare(b.nombre)));
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(mensajeError(err, 'No pudimos cargar los patrocinadores'));
        this.cargando.set(false);
      },
    });
  }

  protected editar(patrocinador: Patrocinador): void {
    this.editando.set(patrocinador);
    this.nombre = patrocinador.nombre;
    this.tipo = patrocinador.tipo ?? '';
    this.contacto = patrocinador.contacto ?? '';
    this.errores.set({});
    this.errorForm.set('');
  }

  protected limpiar(): void {
    this.editando.set(null);
    this.nombre = '';
    this.tipo = '';
    this.contacto = '';
    this.errores.set({});
    this.errorForm.set('');
    this.cambios.markForCheck();
  }

  protected guardar(): void {
    const errores: Errores = {};
    const original = this.editando();

    if (esVacio(this.nombre)) {
      errores['nombre'] = 'Escribe el nombre';
    }

    this.errores.set(errores);
    this.errorForm.set('');

    if (hayErrores(errores) || this.enviando()) {
      return;
    }

    const patrocinador = {
      nombre: this.nombre.trim(),
      tipo: this.tipo.trim(),
      contacto: this.contacto.trim(),
    };
    const peticion = original
      ? this.patrocinioService.actualizarPatrocinador(original.idPatrocinador, patrocinador)
      : this.patrocinioService.crearPatrocinador(patrocinador);

    this.enviando.set(true);

    peticion.subscribe({
      next: () => {
        this.enviando.set(false);
        this.toastService.success(original ? 'Patrocinador actualizado' : 'Patrocinador creado');
        this.limpiar();
        this.cargar();
      },
      error: (err: HttpErrorResponse) => {
        this.enviando.set(false);
        this.errorForm.set(mensajeError(err, 'No se pudo guardar el patrocinador'));
      },
    });
  }

  protected confirmarEliminacion(): void {
    const patrocinador = this.eliminando();

    this.eliminando.set(null);

    if (patrocinador == null) {
      return;
    }

    this.patrocinioService.eliminarPatrocinador(patrocinador.idPatrocinador).subscribe({
      next: () => {
        this.toastService.success('Patrocinador eliminado');

        if (this.editando()?.idPatrocinador === patrocinador.idPatrocinador) {
          this.limpiar();
        }

        this.cargar();
      },
      error: (err: HttpErrorResponse) =>
        this.toastService.error('No se pudo eliminar', mensajeError(err, 'Inténtalo de nuevo.')),
    });
  }
}
