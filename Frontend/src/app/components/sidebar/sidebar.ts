import { Component, computed, inject, input, output, signal } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';
import { ThemeService } from '../../services/theme.service';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog';

export type Panel = 'inicio' | 'festivales' | 'boletas';

@Component({
  selector: 'app-sidebar',
  imports: [ConfirmDialogComponent],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class SidebarComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  protected themeService = inject(ThemeService);

  readonly panelActivo = input<Panel>('inicio');
  readonly panelActivoChange = output<Panel>();

  protected readonly opciones: { panel: Panel; etiqueta: string; icono: string }[] = [
    { panel: 'inicio', etiqueta: 'Inicio', icono: 'pi-home' },
    { panel: 'festivales', etiqueta: 'Festivales', icono: 'pi-calendar' },
    { panel: 'boletas', etiqueta: 'Mis boletas', icono: 'pi-ticket' },
  ];

  protected confirmandoSalida = signal(false);

  protected nombreUsuario = computed(() => this.authService.sesion()?.nombreUsuario ?? 'Usuario');

  protected iniciales = computed(() => this.nombreUsuario().slice(0, 2).toUpperCase());

  protected cerrarSesion(): void {
    this.confirmandoSalida.set(false);
    this.authService.cerrarSesion();
    this.router.navigate(['']);
  }
}
