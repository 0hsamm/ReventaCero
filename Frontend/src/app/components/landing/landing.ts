import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { ANUNCIOS, FESTIVAL } from '../../data/contenido-landing';
import { AuthService } from '../../services/auth.service';
import { ThemeService } from '../../services/theme.service';
import { BoleteriaSectionComponent } from '../boleteria-section/boleteria-section';
import { LineupSectionComponent } from '../lineup-section/lineup-section';
import { MarqueeComponent } from '../marquee/marquee';
import { SiteFooterComponent } from '../site-footer/site-footer';

@Component({
  selector: 'app-landing',
  imports: [
    RouterLink,
    MarqueeComponent,
    LineupSectionComponent,
    BoleteriaSectionComponent,
    SiteFooterComponent,
  ],
  templateUrl: './landing.html',
  styleUrl: './landing.css',
})
export class LandingComponent {
  private authService = inject(AuthService);
  protected themeService = inject(ThemeService);

  protected readonly festival = FESTIVAL;
  protected readonly anuncios = ANUNCIOS;

  protected logueado = computed(() => this.authService.sesion() != null);
}
