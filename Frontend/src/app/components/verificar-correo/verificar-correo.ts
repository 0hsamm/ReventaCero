import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { AuthService } from '../../services/auth.service';
import { esVacio, mensajeError } from '../../utils/validaciones';
import { AuthShellComponent } from '../auth-shell/auth-shell';
import { CampoComponent } from '../campo/campo';

type Estado = 'revisar' | 'verificando' | 'verificada' | 'error';

const TITULOS: Record<Estado, string> = {
  revisar: 'Revisa tu correo',
  verificando: 'Verificando…',
  verificada: 'Correo verificado',
  error: 'Enlace inválido',
};

@Component({
  selector: 'app-verificar-correo',
  imports: [FormsModule, RouterLink, CampoComponent, AuthShellComponent],
  templateUrl: './verificar-correo.html',
})
export class VerificarCorreoComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  protected estado = signal<Estado>('revisar');
  protected titulo = computed(() => TITULOS[this.estado()]);
  protected mensaje = signal('');

  protected aviso = signal('');
  protected usuario = '';

  protected enviando = signal(false);
  protected errorReenvio = signal('');
  protected respuestaReenvio = signal('');

  ngOnInit(): void {
    const datos = history.state as { usuario?: string; aviso?: string } | null;

    this.usuario = datos?.usuario ?? '';
    this.aviso.set(datos?.aviso ?? '');

    const token = this.route.snapshot.queryParamMap.get('token');

    if (token) {
      this.verificar(token);
    }
  }

  private verificar(token: string): void {
    this.estado.set('verificando');
    this.router.navigate([], { relativeTo: this.route, queryParams: {}, replaceUrl: true });

    this.authService.verificarCorreo(token).subscribe({
      next: (mensaje) => {
        this.mensaje.set(mensaje);
        this.estado.set('verificada');
      },
      error: (err: HttpErrorResponse) => {
        this.mensaje.set(mensajeError(err, 'No pudimos verificar tu correo'));
        this.estado.set('error');
      },
    });
  }

  protected reenviar(): void {
    this.errorReenvio.set('');
    this.respuestaReenvio.set('');

    if (esVacio(this.usuario)) {
      this.errorReenvio.set('Escribe tu correo o tu nombre de usuario');
      return;
    }

    if (this.enviando()) {
      return;
    }

    this.enviando.set(true);

    this.authService.reenviarVerificacion(this.usuario.trim()).subscribe({
      next: (respuesta) => {
        this.enviando.set(false);
        this.respuestaReenvio.set(respuesta);
      },
      error: (err: HttpErrorResponse) => {
        this.enviando.set(false);
        this.errorReenvio.set(mensajeError(err, 'No pudimos enviar el correo'));
      },
    });
  }
}
