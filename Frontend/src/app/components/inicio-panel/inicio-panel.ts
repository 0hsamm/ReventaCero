import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, output, signal } from '@angular/core';
import { forkJoin } from 'rxjs';

import { BoletaDetalle } from '../../models/boleta';
import { ETIQUETA_ESTADO, Festival, diasHasta, estadoFestival } from '../../models/festival';
import { AuthService } from '../../services/auth.service';
import { BoletaService } from '../../services/boleta.service';
import { FestivalService } from '../../services/festival.service';
import { BoletaCardComponent } from '../boleta-card/boleta-card';
import { Panel } from '../sidebar/sidebar';

@Component({
  selector: 'app-inicio-panel',
  imports: [DatePipe, BoletaCardComponent],
  templateUrl: './inicio-panel.html',
  styleUrl: './inicio-panel.css',
})
export class InicioPanelComponent implements OnInit {
  private authService = inject(AuthService);
  private boletaService = inject(BoletaService);
  private festivalService = inject(FestivalService);

  readonly abrirPanel = output<Panel>();
  readonly abrirFestival = output<number>();

  protected readonly etiquetaEstado = ETIQUETA_ESTADO;
  protected readonly estadoFestival = estadoFestival;

  protected readonly hoy = new Date();
  protected nombreUsuario = computed(() => this.authService.sesion()?.nombreUsuario ?? '');

  protected cargando = signal(true);
  protected error = signal(false);

  private boletas = signal<BoletaDetalle[]>([]);
  private festivales = signal<Festival[]>([]);

  protected proximaBoleta = computed(
    () =>
      this.boletas()
        .filter((boleta) => estadoFestival(boleta.festival) !== 'finalizado')
        .sort((a, b) => a.festival.fechaInicio.localeCompare(b.festival.fechaInicio))[0] ?? null,
  );

  protected proximosFestivales = computed(() =>
    this.festivales()
      .filter((festival) => estadoFestival(festival) !== 'finalizado')
      .sort((a, b) => a.fechaInicio.localeCompare(b.fechaInicio))
      .slice(0, 4),
  );

  protected tiempoRestante = computed(() => {
    const boleta = this.proximaBoleta();

    if (boleta == null) {
      return '';
    }

    const dias = diasHasta(boleta.festival.fechaInicio);

    if (dias <= 0) {
      return 'El festival está en curso';
    }

    return dias === 1 ? 'Empieza mañana' : `Empieza en ${dias} días`;
  });

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set(false);

    forkJoin({
      boletas: this.boletaService.listarDetallePorComprador(
        this.authService.sesion()?.documento ?? 0,
      ),
      festivales: this.festivalService.listar(),
    }).subscribe({
      next: ({ boletas, festivales }) => {
        this.boletas.set(boletas);
        this.festivales.set(festivales);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set(true);
        this.cargando.set(false);
      },
    });
  }
}
