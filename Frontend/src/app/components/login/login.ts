import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { hoyIso } from '../../models/festival';
import { AuthService } from '../../services/auth.service';
import { ThemeService } from '../../services/theme.service';
import { ToastService } from '../../services/toast.service';

const LONGITUD_MINIMA_CONTRASENIA = 8;

type Errores = Partial<
  Record<
    | 'documento'
    | 'nombreUsuario'
    | 'contrasenia'
    | 'nombre'
    | 'apellido'
    | 'correo'
    | 'fechaNacimiento',
    string
  >
>;

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private toastService = inject(ToastService);
  protected themeService = inject(ThemeService);

  protected readonly longitudMinima = LONGITUD_MINIMA_CONTRASENIA;
  protected readonly hoy = hoyIso();

  protected modoRegistro = signal(false);
  protected cargando = signal(false);
  protected error = signal('');
  protected errores = signal<Errores>({});

  protected documento = '';
  protected nombreUsuario = '';
  protected contrasenia = '';
  protected nombre = '';
  protected apellido = '';
  protected correo = '';
  protected fechaNacimiento = '';
  protected telefono = '';

  ngOnInit(): void {
    if (this.authService.estaLogueado()) {
      this.router.navigate(['/app']);
    }
  }

  protected cambiarModo(registro: boolean): void {
    this.modoRegistro.set(registro);
    this.error.set('');
    this.errores.set({});
  }

  protected entrar(): void {
    const errores: Errores = {};

    if (this.nombreUsuario.trim() === '') {
      errores.nombreUsuario = 'Escribe tu nombre de usuario';
    }

    if (this.contrasenia === '') {
      errores.contrasenia = 'Escribe tu contraseña';
    }

    if (!this.validar(errores)) {
      return;
    }

    this.cargando.set(true);

    this.authService
      .login({ nombreUsuario: this.nombreUsuario.trim(), contrasenia: this.contrasenia })
      .subscribe({
        next: () => {
          this.cargando.set(false);
          this.router.navigate(['/app']);
        },
        error: (err: HttpErrorResponse) =>
          this.mostrarError(err, 'Usuario o contraseña incorrectos'),
      });
  }

  protected registrar(): void {
    const errores: Errores = {};

    if (!/^[1-9]\d*$/.test(this.documento.trim())) {
      errores.documento = 'Escribe solo números, sin puntos ni espacios';
    }

    if (this.nombre.trim() === '') {
      errores.nombre = 'Escribe tu nombre';
    }

    if (this.apellido.trim() === '') {
      errores.apellido = 'Escribe tu apellido';
    }

    if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(this.correo.trim())) {
      errores.correo = 'Escribe un correo válido, como nombre@correo.com';
    }

    if (this.fechaNacimiento === '') {
      errores.fechaNacimiento = 'Selecciona tu fecha de nacimiento';
    }

    if (this.nombreUsuario.trim() === '') {
      errores.nombreUsuario = 'Elige un nombre de usuario';
    }

    if (this.contrasenia.length < LONGITUD_MINIMA_CONTRASENIA) {
      errores.contrasenia = `Usa al menos ${LONGITUD_MINIMA_CONTRASENIA} caracteres`;
    }

    if (!this.validar(errores)) {
      return;
    }

    this.cargando.set(true);

    this.authService
      .registro({
        documento: Number(this.documento.trim()),
        nombreUsuario: this.nombreUsuario.trim(),
        contrasenia: this.contrasenia,
        nombre: this.nombre.trim(),
        apellido: this.apellido.trim(),
        correo: this.correo.trim(),
        fechaNacimiento: this.fechaNacimiento,
        telefono: this.telefono.trim() ? [this.telefono.trim()] : [],
      })
      .subscribe({
        next: () => {
          this.cargando.set(false);
          this.contrasenia = '';
          this.cambiarModo(false);
          this.toastService.success('Cuenta creada', 'Ya puedes iniciar sesión.');
        },
        error: (err: HttpErrorResponse) => this.mostrarError(err, 'No se pudo crear la cuenta'),
      });
  }

  private validar(errores: Errores): boolean {
    this.error.set('');
    this.errores.set(errores);

    return Object.keys(errores).length === 0;
  }

  private mostrarError(err: HttpErrorResponse, porDefecto: string): void {
    this.cargando.set(false);

    if (err.status === 0) {
      this.error.set('No hay conexión con el servidor. Inténtalo de nuevo en unos minutos.');
    } else {
      this.error.set(typeof err.error === 'string' && err.error ? err.error : porDefecto);
    }
  }
}
