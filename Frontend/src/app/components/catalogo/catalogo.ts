import { Location } from '@angular/common';
import { Component, OnInit, computed, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { Festival, estadoFestival } from '../../models/modelos';
import { FestivalService } from '../../services/festival.service';
import { EventoCardComponent } from '../evento-card/evento-card';

function normalizar(texto: string): string {
  return texto
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase()
    .trim();
}

@Component({
  selector: 'app-catalogo',
  imports: [FormsModule, EventoCardComponent],
  templateUrl: './catalogo.html',
  styleUrl: './catalogo.css',
})
export class CatalogoComponent implements OnInit {
  private festivalService = inject(FestivalService);
  private location = inject(Location);
  private parametros = inject(ActivatedRoute).snapshot.queryParamMap;

  protected cargando = signal(true);
  protected error = signal(false);
  private festivales = signal<Festival[]>([]);

  protected busqueda = signal(this.parametros.get('q') ?? '');
  protected ciudad = signal(this.parametros.get('ciudad') ?? '');
  protected desde = signal(this.parametros.get('desde') ?? '');

  // Los filtros viven en la URL: al volver de un festival siguen aplicados
  private sincronizarUrl = effect(() => {
    const filtros = new URLSearchParams(
      Object.entries({ q: this.busqueda(), ciudad: this.ciudad(), desde: this.desde() }).filter(
        ([, valor]) => valor,
      ),
    );

    this.location.replaceState('/eventos', filtros.toString());
  });

  private vigentes = computed(() =>
    this.festivales().filter((festival) => estadoFestival(festival) !== 'finalizado'),
  );

  protected ciudades = computed(() =>
    [...new Set(this.vigentes().map((festival) => festival.ubicacionCiudad))].sort(),
  );

  protected resultados = computed(() => {
    const busqueda = normalizar(this.busqueda());

    return this.vigentes()
      .filter(
        (festival) =>
          normalizar(`${festival.nombre} ${festival.ubicacionCiudad}`).includes(busqueda) &&
          (!this.ciudad() || festival.ubicacionCiudad === this.ciudad()) &&
          (!this.desde() || festival.fechaFin >= this.desde()),
      )
      .sort((a, b) => a.fechaInicio.localeCompare(b.fechaInicio));
  });

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set(false);

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

  protected limpiar(): void {
    this.busqueda.set('');
    this.ciudad.set('');
    this.desde.set('');
  }
}
