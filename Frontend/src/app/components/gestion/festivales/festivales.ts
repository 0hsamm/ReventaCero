import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Festival, Organizador } from '../../../models/modelos';
import { AdminService } from '../../../services/admin.service';
import { AuthService } from '../../../services/auth.service';
import { FestivalService } from '../../../services/festival.service';
import { ToastService } from '../../../services/toast.service';
import { subirPanel } from '../../../utils/panel';
import { Errores, esVacio, hayErrores, mensajeError } from '../../../utils/validaciones';
import { CampoComponent } from '../../campo/campo';
import { ConfirmDialogComponent } from '../../confirm-dialog/confirm-dialog';
import { PaginacionComponent, TAMANO_PAGINA } from '../../paginacion/paginacion';

const VACIO: Partial<Festival> = {
  nombre: '',
  edicion: undefined,
  fechaInicio: '',
  fechaFin: '',
  ubicacionPais: '',
  ubicacionCiudad: '',
  ubicacionDireccion: '',
  documentoOrganizador: undefined,
};

@Component({
  selector: 'app-gestion-festivales',
  imports: [
    DatePipe,
    FormsModule,
    RouterLink,
    CampoComponent,
    ConfirmDialogComponent,
    PaginacionComponent,
  ],
  templateUrl: './festivales.html',
})
export class GestionFestivalesComponent implements OnInit {
  private festivalService = inject(FestivalService);
  private adminService = inject(AdminService);
  private authService = inject(AuthService);
  private toastService = inject(ToastService);

  protected esAdmin = computed(() => this.authService.sesion()?.tipoUsuario === 'ADMINISTRADOR');

  protected cargando = signal(true);
  protected error = signal('');
  protected festivales = signal<Festival[]>([]);
  protected organizadores = signal<Organizador[]>([]);
  protected busqueda = signal('');
  protected pagina = signal(1);

  protected formularioAbierto = signal(false);
  protected editando = signal<Festival | null>(null);
  protected eliminando = signal<Festival | null>(null);
  protected enviando = signal(false);
  protected errorForm = signal('');
  protected errores = signal<Errores>({});
  protected form: Partial<Festival> = { ...VACIO };

  private filtrados = computed(() => {
    const busqueda = this.busqueda().trim().toLowerCase();

    return this.festivales().filter((f) =>
      `${f.nombre} ${f.ubicacionCiudad}`.toLowerCase().includes(busqueda),
    );
  });

  protected total = computed(() => this.filtrados().length);
  protected filas = computed(() =>
    this.filtrados().slice((this.pagina() - 1) * TAMANO_PAGINA, this.pagina() * TAMANO_PAGINA),
  );

  ngOnInit(): void {
    this.cargar();

    if (this.esAdmin()) {
      this.adminService.organizadores().subscribe({
        next: (lista) => this.organizadores.set(lista),
        error: () => this.organizadores.set([]),
      });
    }
  }

  protected cargar(): void {
    const documento = this.authService.sesion()?.documento ?? 0;
    const peticion = this.esAdmin()
      ? this.festivalService.listar()
      : this.festivalService.listarPorOrganizador(documento);

    this.cargando.set(true);
    this.error.set('');

    peticion.subscribe({
      next: (lista) => {
        this.festivales.set([...lista].sort((a, b) => b.fechaInicio.localeCompare(a.fechaInicio)));
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(mensajeError(err, 'No pudimos cargar los festivales'));
        this.cargando.set(false);
      },
    });
  }

  protected buscar(valor: string): void {
    this.busqueda.set(valor);
    this.pagina.set(1);
  }

  protected nuevo(): void {
    this.abrir({ ...VACIO }, null);
  }

  protected editar(festival: Festival): void {
    this.abrir({ ...festival }, festival);
  }

  protected guardar(): void {
    const errores = this.validar();
    const original = this.editando();

    this.errores.set(errores);
    this.errorForm.set('');

    if (hayErrores(errores) || this.enviando()) {
      return;
    }

    this.enviando.set(true);

    const peticion = original
      ? this.festivalService.actualizar(original.idFestival, this.form)
      : this.festivalService.crear(this.form);

    peticion.subscribe({
      next: () => {
        this.enviando.set(false);
        this.toastService.success(original ? 'Festival actualizado' : 'Festival creado');
        this.formularioAbierto.set(false);
        this.cargar();
      },
      error: (err: HttpErrorResponse) => {
        this.enviando.set(false);
        this.errorForm.set(mensajeError(err, 'No se pudo guardar el festival'));
      },
    });
  }

  protected confirmarEliminacion(): void {
    const festival = this.eliminando();

    this.eliminando.set(null);

    if (festival == null) {
      return;
    }

    this.festivalService.eliminar(festival.idFestival).subscribe({
      next: () => {
        this.toastService.success('Festival eliminado');
        this.cargar();
      },
      error: (err: HttpErrorResponse) =>
        this.toastService.error('No se pudo eliminar', mensajeError(err, 'Inténtalo de nuevo.')),
    });
  }

  private abrir(form: Partial<Festival>, festival: Festival | null): void {
    this.form = form;
    this.editando.set(festival);
    this.errores.set({});
    this.errorForm.set('');
    this.formularioAbierto.set(true);
    subirPanel();
  }

  private validar(): Errores {
    const errores: Errores = {};
    const formulario = this.form;

    if (esVacio(formulario.nombre)) {
      errores['nombre'] = 'Escribe el nombre';
    }

    if (
      formulario.edicion == null ||
      formulario.edicion <= 0 ||
      !Number.isInteger(formulario.edicion)
    ) {
      errores['edicion'] = 'Escribe la edición como un número entero';
    }

    if (esVacio(formulario.fechaInicio)) {
      errores['fechaInicio'] = 'Selecciona la fecha de inicio';
    }

    if (
      esVacio(formulario.fechaFin) ||
      (formulario.fechaFin ?? '') < (formulario.fechaInicio ?? '')
    ) {
      errores['fechaFin'] = 'Debe ser igual o posterior al inicio';
    }

    if (esVacio(formulario.ubicacionPais)) {
      errores['ubicacionPais'] = 'Escribe el país';
    }

    if (esVacio(formulario.ubicacionCiudad)) {
      errores['ubicacionCiudad'] = 'Escribe la ciudad';
    }

    if (this.esAdmin() && !formulario.documentoOrganizador) {
      errores['documentoOrganizador'] = 'Selecciona el organizador';
    }

    return errores;
  }
}
