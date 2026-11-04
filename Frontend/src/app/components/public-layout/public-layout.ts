import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from '../../services/auth.service';
import { ThemeService } from '../../services/theme.service';
import { SiteFooterComponent } from '../site-footer/site-footer';

@Component({
  selector: 'app-public-layout',
  imports: [RouterLink, RouterLinkActive, RouterOutlet, SiteFooterComponent],
  templateUrl: './public-layout.html',
  styleUrl: './public-layout.css',
})
export class PublicLayoutComponent {
  private authService = inject(AuthService);
  protected themeService = inject(ThemeService);

  protected logueado = computed(() => this.authService.sesion() != null);
  protected rutaPanel = computed(() => this.authService.rutaInicio());
}
