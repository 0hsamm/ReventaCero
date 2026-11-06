import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { hoyIso } from '../../models/modelos';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';
import {
  Errores,
  LONGITUD_MINIMA_CONTRASENIA,
  esCorreo,
  esDocumento,
  esTelefono,
  esVacio,
  hayErrores,
  limpiarTelefono,
  mensajeError,
  tieneLetras,
} from '../../utils/validaciones';
import { AuthShellComponent } from '../auth-shell/auth-shell';
import { CampoComponent } from '../campo/campo';

// El registro público solo crea cuentas de usuario: el backend crea a organizadores y
// administradores desde el panel de administración.
@Component({
  selector: 'app-registro',
  imports: [FormsModule, RouterLink, CampoComponent, AuthShellComponent],
  templateUrl: './registro.html',
})
export class RegistroComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private toastService = inject(ToastService);

  protected readonly longitudMinima = LONGITUD_MINIMA_CONTRASENIA;
  protected readonly hoy = hoyIso();

  protected cargando = signal(false);
  protected error = signal('');
  protected errores = signal<Errores>({});

  protected documento = '';
  protected nombre = '';
  protected apellido = '';
  protected correo = '';
  protected telefono = '';
  protected fechaNacimiento = '';
  protected nombreUsuario = '';
  protected contrasenia = '';
  protected confirmacion = '';

  ngOnInit(): void {
    if (this.authService.sesion()) {
      this.router.navigateByUrl(this.authService.rutaInicio());
    }
  }

  protected registrar(): void {
    const errores = this.validar();

    this.error.set('');
    this.errores.set(errores);

    if (hayErrores(errores) || this.cargando()) {
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
        telefono: this.telefono.trim() ? [limpiarTelefono(this.telefono)] : [],
      })
      .subscribe({
        next: (mensaje) => {
          this.cargando.set(false);
          this.toastService.success('Cuenta creada', 'Verifica tu correo para activarla.');
          this.router.navigate(['/verificar-correo'], {
            state: { usuario: this.correo.trim(), aviso: mensaje },
          });
        },
        error: (err: HttpErrorResponse) => {
          this.cargando.set(false);
          this.error.set(mensajeError(err, 'No se pudo crear la cuenta'));
        },
      });
  }

  private validar(): Errores {
    const errores: Errores = {};

    if (!esDocumento(this.documento)) {
      errores['documento'] = 'Escribe de 5 a 15 dígitos, sin puntos ni letras';
    }

    if (!tieneLetras(this.nombre)) {
      errores['nombre'] = 'Escribe tu nombre';
    }

    if (!tieneLetras(this.apellido)) {
      errores['apellido'] = 'Escribe tu apellido';
    }

    if (!esCorreo(this.correo)) {
      errores['correo'] = 'Escribe un correo válido, como nombre@correo.com';
    }

    if (this.telefono.trim() && !esTelefono(this.telefono)) {
      errores['telefono'] = 'Escribe un teléfono de 7 a 15 dígitos';
    }

    if (esVacio(this.fechaNacimiento) || this.fechaNacimiento >= this.hoy) {
      errores['fechaNacimiento'] = 'Selecciona una fecha anterior a hoy';
    }

    if (esVacio(this.nombreUsuario)) {
      errores['nombreUsuario'] = 'Elige un nombre de usuario';
    }

    if (esVacio(this.contrasenia) || this.contrasenia.length < LONGITUD_MINIMA_CONTRASENIA) {
      errores['contrasenia'] = `Usa al menos ${LONGITUD_MINIMA_CONTRASENIA} caracteres`;
    }

    if (esVacio(this.confirmacion) || this.confirmacion !== this.contrasenia) {
      errores['confirmacion'] = 'Las contraseñas no coinciden';
    }

    return errores;
  }
}
