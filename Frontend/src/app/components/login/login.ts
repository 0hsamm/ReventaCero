import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { AuthService } from '../../services/auth.service';
import { ThemeService } from '../../services/theme.service';
import { Errores, esVacio, hayErrores, mensajeError } from '../../utils/validaciones';
import { CampoComponent } from '../campo/campo';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink, CampoComponent],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  protected themeService = inject(ThemeService);

  protected cargando = signal(false);
  protected error = signal('');
  protected errores = signal<Errores>({});

  protected nombreUsuario = '';
  protected contrasenia = '';

  ngOnInit(): void {
    if (this.authService.validarSesion()) {
      this.router.navigateByUrl(this.authService.rutaInicio());
    }
  }

  protected entrar(): void {
    const errores: Errores = {};

    if (esVacio(this.nombreUsuario)) {
      errores['nombreUsuario'] = 'Escribe tu nombre de usuario';
    }

    if (esVacio(this.contrasenia)) {
      errores['contrasenia'] = 'Escribe tu contraseña';
    }

    this.error.set('');
    this.errores.set(errores);

    if (hayErrores(errores) || this.cargando()) {
      return;
    }

    this.cargando.set(true);

    this.authService
      .login({ nombreUsuario: this.nombreUsuario.trim(), contrasenia: this.contrasenia })
      .subscribe({
        next: () => {
          this.cargando.set(false);
          this.irDespuesDeEntrar();
        },
        error: (err: unknown) => {
          this.cargando.set(false);
          this.error.set(
            err instanceof HttpErrorResponse
              ? mensajeError(err, 'Usuario o contraseña incorrectos')
              : err instanceof Error
                ? err.message
                : 'No pudimos iniciar sesión. Inténtalo de nuevo.',
          );
        },
      });
  }

  private irDespuesDeEntrar(): void {
    const volver = this.route.snapshot.queryParamMap.get('volver') ?? '';
    // Solo rutas internas: "//sitio" se interpretaría como otro dominio
    const interno = volver.startsWith('/') && !volver.startsWith('//');
    // El retorno a la compra corresponde al usuario; gestión inicia en su propio panel.
    const volverComoUsuario = this.authService.sesion()?.tipoUsuario === 'USUARIO' && interno;

    this.router.navigateByUrl(volverComoUsuario ? volver : this.authService.rutaInicio());
  }
}
