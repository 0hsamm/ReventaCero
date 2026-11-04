import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Usuario, hoyIso } from '../../../models/modelos';
import { AdminService } from '../../../services/admin.service';
import { AuthService } from '../../../services/auth.service';
import { ToastService } from '../../../services/toast.service';
import { subirPanel } from '../../../utils/panel';
import {
  Errores,
  LONGITUD_MINIMA_CONTRASENIA,
  esCorreo,
  esDocumento,
  esVacio,
  hayErrores,
  mensajeError,
  tieneLetras,
} from '../../../utils/validaciones';
import { CampoComponent } from '../../campo/campo';
import { ConfirmDialogComponent } from '../../confirm-dialog/confirm-dialog';
import { PaginacionComponent, TAMANO_PAGINA } from '../../paginacion/paginacion';

interface CambioEstado {
  usuario: Usuario;
  estado: string;
}

const VACIO = {
  documento: '',
  nombre: '',
  apellido: '',
  correo: '',
  fechaNacimiento: '',
  nombreUsuario: '',
  contrasenia: '',
  cargo: '',
};

function claseEstado(estado?: string): string {
  if (estado === 'ACTIVA') {
    return 'chip-ok';
  }

  return estado === 'BLOQUEADA' ? 'chip-alerta' : 'chip-neutro';
}

@Component({
  selector: 'app-admin-usuarios',
  imports: [FormsModule, CampoComponent, ConfirmDialogComponent, PaginacionComponent],
  templateUrl: './usuarios.html',
})
export class AdminUsuariosComponent implements OnInit {
  private adminService = inject(AdminService);
  private authService = inject(AuthService);
  private toastService = inject(ToastService);

  protected readonly estados = ['ACTIVA', 'SUSPENDIDA', 'BLOQUEADA'];
  protected readonly roles = ['USUARIO', 'ORGANIZADOR', 'ADMINISTRADOR'];
  protected readonly hoy = hoyIso();
  protected readonly claseEstado = claseEstado;

  protected yo = computed(() => this.authService.sesion()?.documento);

  protected cargando = signal(true);
  protected error = signal('');
  protected usuarios = signal<Usuario[]>([]);
  protected busqueda = signal('');
  protected filtroRol = signal('');
  protected pagina = signal(1);

  protected cambio = signal<CambioEstado | null>(null);
  protected formularioAbierto = signal(false);
  protected errorForm = signal('');
  protected errores = signal<Errores>({});
  protected form = { ...VACIO };

  private filtrados = computed(() => {
    const busqueda = this.busqueda().trim().toLowerCase();

    return this.usuarios().filter(
      (u) =>
        `${u.nombre} ${u.apellido} ${u.nombreUsuario} ${u.correo} ${u.documento}`
          .toLowerCase()
          .includes(busqueda) &&
        (!this.filtroRol() || u.tipoUsuario === this.filtroRol()),
    );
  });

  protected total = computed(() => this.filtrados().length);
  protected filas = computed(() =>
    this.filtrados().slice((this.pagina() - 1) * TAMANO_PAGINA, this.pagina() * TAMANO_PAGINA),
  );

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    this.adminService.usuarios().subscribe({
      next: (lista) => {
        this.usuarios.set(lista);
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(mensajeError(err, 'No pudimos cargar los usuarios'));
        this.cargando.set(false);
      },
    });
  }

  protected filtrar(campo: 'busqueda' | 'rol', valor: string): void {
    (campo === 'busqueda' ? this.busqueda : this.filtroRol).set(valor);
    this.pagina.set(1);
  }

  protected confirmarCambio(): void {
    const cambio = this.cambio();

    this.cambio.set(null);

    if (cambio == null) {
      return;
    }

    this.adminService.cambiarEstadoUsuario(cambio.usuario.documento, cambio.estado).subscribe({
      next: () => {
        this.toastService.success('Estado actualizado');
        this.cargar();
      },
      error: (err: HttpErrorResponse) =>
        this.toastService.error('No se pudo cambiar', mensajeError(err, 'Inténtalo de nuevo.')),
    });
  }

  protected abrirFormulario(): void {
    this.form = { ...VACIO };
    this.errores.set({});
    this.errorForm.set('');
    this.formularioAbierto.set(true);
    subirPanel();
  }

  protected crearOrganizador(): void {
    const errores = this.validar();

    this.errores.set(errores);
    this.errorForm.set('');

    if (hayErrores(errores)) {
      return;
    }

    this.adminService
      .crearOrganizador({
        ...this.form,
        documento: Number(this.form.documento),
        nombre: this.form.nombre.trim(),
        apellido: this.form.apellido.trim(),
        correo: this.form.correo.trim(),
        nombreUsuario: this.form.nombreUsuario.trim(),
        cargo: this.form.cargo.trim(),
        telefono: [],
        autorizadoPorDocumento: this.yo(),
      })
      .subscribe({
        next: () => {
          this.toastService.success('Organizador creado');
          this.formularioAbierto.set(false);
          this.cargar();
        },
        error: (err: HttpErrorResponse) =>
          this.errorForm.set(mensajeError(err, 'No se pudo crear el organizador')),
      });
  }

  private validar(): Errores {
    const errores: Errores = {};
    const formulario = this.form;

    if (!esDocumento(formulario.documento)) {
      errores['documento'] = 'Escribe de 5 a 15 dígitos';
    }

    if (!tieneLetras(formulario.nombre)) {
      errores['nombre'] = 'Escribe el nombre';
    }

    if (!tieneLetras(formulario.apellido)) {
      errores['apellido'] = 'Escribe el apellido';
    }

    if (!esCorreo(formulario.correo)) {
      errores['correo'] = 'Escribe un correo válido';
    }

    if (esVacio(formulario.fechaNacimiento) || formulario.fechaNacimiento >= this.hoy) {
      errores['fechaNacimiento'] = 'Selecciona una fecha anterior a hoy';
    }

    if (esVacio(formulario.nombreUsuario)) {
      errores['nombreUsuario'] = 'Elige un nombre de usuario';
    }

    if (
      esVacio(formulario.contrasenia) ||
      formulario.contrasenia.length < LONGITUD_MINIMA_CONTRASENIA
    ) {
      errores['contrasenia'] = `Usa al menos ${LONGITUD_MINIMA_CONTRASENIA} caracteres`;
    }

    if (esVacio(formulario.cargo)) {
      errores['cargo'] = 'Escribe el cargo';
    }

    return errores;
  }
}
