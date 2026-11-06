import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from '../../services/auth.service';
import { ThemeService } from '../../services/theme.service';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog';
import { SiteFooterComponent } from '../site-footer/site-footer';

@Component({
  selector: 'app-public-layout',
  imports: [RouterLink, RouterLinkActive, RouterOutlet, SiteFooterComponent, ConfirmDialogComponent],
  templateUrl: './public-layout.html',
  styleUrl: './public-layout.css',
})
export class PublicLayoutComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  protected themeService = inject(ThemeService);

  protected logueado = computed(() => this.authService.sesion() != null);
  protected rutaPanel = computed(() => this.authService.rutaInicio());
  protected confirmandoSalida = signal(false);

  protected cerrarSesion(): void {
    this.confirmandoSalida.set(false);
    this.authService.cerrarSesion();
    this.router.navigate(['']);
  }
}
