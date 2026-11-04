import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';

import { Dispositivo, ETIQUETA_ROL } from '../../models/modelos';
import { AuthService } from '../../services/auth.service';
import { BoletaService } from '../../services/boleta.service';
import { ToastService } from '../../services/toast.service';
import { mensajeError } from '../../utils/validaciones';

// Solo lectura: el backend reserva /usuarios para administradores, así que nadie puede
// consultar ni editar sus propios datos personales desde su cuenta.
@Component({
  selector: 'app-perfil',
  template: `
    <div class="pagina-panel">
      <header class="panel-header">
        <h1>Mi perfil</h1>
        <p>Datos de tu cuenta y dispositivos autorizados.</p>
      </header>

      <section class="tarjeta-panel">
        <h2>Cuenta</h2>
        <table class="tabla">
          <tbody>
            <tr><td>Nombre de usuario</td><td>{{ sesion()?.nombreUsuario }}</td></tr>
            <tr><td>Documento</td><td>{{ sesion()?.documento }}</td></tr>
            <tr><td>Correo</td><td>{{ sesion()?.correo }}</td></tr>
            <tr><td>Rol</td><td>{{ rol() }}</td></tr>
          </tbody>
        </table>
      </section>

      <section class="tarjeta-panel">
        <h2>Dispositivos</h2>
        <p class="nota intro">Cada compra, reventa o devolución queda asociada a un dispositivo.</p>

        @if (error()) {
          <p class="error-general" role="alert">{{ error() }}</p>
        } @else if (dispositivos().length === 0) {
          <p class="nota">Este navegador se registrará en tu primera transacción.</p>
        } @else {
          <table class="tabla">
            <tbody>
              @for (item of dispositivos(); track item.idDispositivo) {
                <tr>
                  <td>{{ item.tipo }}</td>
                  <td>{{ item.identificadorUnico }}</td>
                  <td>
                    <span class="chip" [class]="item.activo ? 'chip-ok' : 'chip-neutro'">
                      {{ item.activo ? 'Activo' : 'Inactivo' }}
                    </span>
                  </td>
                  <td>
                    <button type="button" class="btn btn-ghost btn-sm" (click)="alternar(item)">
                      {{ item.activo ? 'Desactivar' : 'Activar' }}
                    </button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        }
      </section>
    </div>
  `,
  styles: `
    .intro {
      margin-bottom: 12px;
    }
  `,
})
export class PerfilComponent implements OnInit {
  private authService = inject(AuthService);
  private boletaService = inject(BoletaService);
  private toastService = inject(ToastService);

  protected sesion = this.authService.sesion;
  protected rol = computed(() => ETIQUETA_ROL[this.sesion()?.tipoUsuario ?? 'USUARIO']);

  protected dispositivos = signal<Dispositivo[]>([]);
  protected error = signal('');

  ngOnInit(): void {
    this.cargar();
  }

  protected alternar(dispositivo: Dispositivo): void {
    this.boletaService.cambiarEstadoDispositivo(dispositivo.idDispositivo, !dispositivo.activo).subscribe({
      next: () => this.cargar(),
      error: (err: HttpErrorResponse) =>
        this.toastService.error('No se pudo cambiar', mensajeError(err, 'Inténtalo de nuevo.')),
    });
  }

  private cargar(): void {
    this.boletaService.dispositivos().subscribe({
      next: (lista) => this.dispositivos.set(lista),
      error: (err: HttpErrorResponse) =>
        this.error.set(mensajeError(err, 'No pudimos cargar tus dispositivos')),
    });
  }
}
