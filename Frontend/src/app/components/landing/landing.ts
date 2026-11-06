import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { Festival, estadoFestival } from '../../models/modelos';
import { FestivalService } from '../../services/festival.service';
import { EventoCardComponent } from '../evento-card/evento-card';
import { MarqueeComponent } from '../marquee/marquee';

@Component({
  selector: 'app-landing',
  imports: [FormsModule, RouterLink, EventoCardComponent, MarqueeComponent],
  templateUrl: './landing.html',
  styleUrl: './landing.css',
})
export class LandingComponent implements OnInit {
  private router = inject(Router);
  private festivalService = inject(FestivalService);

  protected readonly anuncios = 'Boletas con código único • Reventa con precio tope • Venta directa • ';

  protected busqueda = '';

  protected cargando = signal(true);
  protected error = signal(false);
  private festivales = signal<Festival[]>([]);

  protected destacados = computed(() =>
    this.festivales()
      .filter((festival) => estadoFestival(festival) !== 'finalizado')
      .sort((a, b) => a.fechaInicio.localeCompare(b.fechaInicio))
      .slice(0, 5),
  );

  ngOnInit(): void {
    this.festivalService.listar().subscribe({
      next: (festivales) => {
        this.festivales.set(festivales);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set(true);
        this.cargando.set(false);
      },
    });
  }

  protected buscar(): void {
    this.router.navigate(['/eventos'], {
      queryParams: this.busqueda.trim() ? { q: this.busqueda.trim() } : {},
    });
  }
}
