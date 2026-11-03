import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';

import {
  DetalleFestival,
  ETIQUETA_ESTADO,
  EstadoFestival,
  Festival,
  estadoFestival,
} from '../../models/festival';
import { FestivalService } from '../../services/festival.service';

@Component({
  selector: 'app-festivales-panel',
  imports: [CurrencyPipe, DatePipe, DecimalPipe, FormsModule],
  templateUrl: './festivales-panel.html',
  styleUrl: './festivales-panel.css',
})
export class FestivalesPanelComponent implements OnInit {
  private festivalService = inject(FestivalService);

  readonly festivalInicial = input<number | null>(null);

  protected readonly etiquetaEstado = ETIQUETA_ESTADO;
  protected readonly estadoFestival = estadoFestival;
  protected readonly filtrosEstado: { valor: EstadoFestival | 'todos'; etiqueta: string }[] = [
    { valor: 'todos', etiqueta: 'Todos' },
    { valor: 'proximo', etiqueta: 'Próximos' },
    { valor: 'en-curso', etiqueta: 'En curso' },
    { valor: 'finalizado', etiqueta: 'Finalizados' },
  ];

  protected cargando = signal(true);
  protected error = signal(false);
  protected festivales = signal<Festival[]>([]);

  protected busqueda = signal('');
  protected ciudad = signal('');
  protected estado = signal<EstadoFestival | 'todos'>('todos');

  protected seleccionado = signal<Festival | null>(null);
  protected detalle = signal<DetalleFestival | null>(null);
  protected cargandoDetalle = signal(false);
  protected errorDetalle = signal(false);

  private peticionDetalle?: Subscription;

  protected ciudades = computed(() =>
    [...new Set(this.festivales().map((festival) => festival.ubicacionCiudad))].sort(),
  );

  protected filtrados = computed(() => {
    const busqueda = this.normalizar(this.busqueda());

    return this.festivales()
      .filter(
        (festival) =>
          this.normalizar(festival.nombre).includes(busqueda) &&
          (this.ciudad() === '' || festival.ubicacionCiudad === this.ciudad()) &&
          (this.estado() === 'todos' || estadoFestival(festival) === this.estado()),
      )
      .sort((a, b) => this.ordenar(a, b));
  });

  protected entradas = computed(() => {
    const detalle = this.detalle();

    return (detalle?.tiposEntrada ?? []).map((tipo) => ({
      tipo,
      zona: detalle?.zonas.find((zona) => zona.idZona === tipo.idZona),
    }));
  });

  protected cartel = computed(() => {
    const detalle = this.detalle();
    const dias = new Map<string, { hora: string; artista: string; pais: string; escenario: string; id: number }[]>();

    [...(detalle?.presentaciones ?? [])]
      .sort((a, b) => a.horaInicio.localeCompare(b.horaInicio))
      .forEach((presentacion) => {
        const artista = detalle?.artistas.find((item) => item.idArtista === presentacion.idArtista);
        const fila = {
          id: presentacion.idPresentacion,
          hora: presentacion.horaInicio,
          artista: artista?.nombreArtistico ?? 'Por confirmar',
          pais: artista?.pais ?? '',
          escenario: presentacion.nombreEscenario,
        };

        dias.set(presentacion.fecha, [...(dias.get(presentacion.fecha) ?? []), fila]);
      });

    return [...dias.entries()].map(([fecha, filas]) => ({ fecha, filas }));
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

        const inicial =
          festivales.find((item) => item.idFestival === this.festivalInicial()) ??
          this.filtrados()[0];

        if (inicial) {
          this.seleccionar(inicial);
        }
      },
      error: () => {
        this.error.set(true);
        this.cargando.set(false);
      },
    });
  }

  protected seleccionar(festival: Festival): void {
    this.seleccionado.set(festival);
    this.detalle.set(null);
    this.cargandoDetalle.set(true);
    this.errorDetalle.set(false);

    this.peticionDetalle?.unsubscribe();
    this.peticionDetalle = this.festivalService.obtenerDetalle(festival.idFestival).subscribe({
      next: (detalle) => {
        this.detalle.set(detalle);
        this.cargandoDetalle.set(false);
      },
      error: () => {
        this.errorDetalle.set(true);
        this.cargandoDetalle.set(false);
      },
    });
  }

  protected limpiarFiltros(): void {
    this.busqueda.set('');
    this.ciudad.set('');
    this.estado.set('todos');
  }

  // Primero lo que sigue vigente, por fecha; al final lo finalizado.
  private ordenar(a: Festival, b: Festival): number {
    const finalizadoA = estadoFestival(a) === 'finalizado';
    const finalizadoB = estadoFestival(b) === 'finalizado';

    if (finalizadoA !== finalizadoB) {
      return finalizadoA ? 1 : -1;
    }

    return finalizadoA
      ? b.fechaInicio.localeCompare(a.fechaInicio)
      : a.fechaInicio.localeCompare(b.fechaInicio);
  }

  private normalizar(texto: string): string {
    return texto
      .normalize('NFD')
      .replace(/\p{Diacritic}/gu, '')
      .toLowerCase()
      .trim();
  }
}
