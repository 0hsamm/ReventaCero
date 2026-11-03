import { Component, OnInit, computed, inject, output, signal } from '@angular/core';

import { BoletaDetalle } from '../../models/boleta';
import { estadoFestival } from '../../models/festival';
import { AuthService } from '../../services/auth.service';
import { BoletaService } from '../../services/boleta.service';
import { BoletaCardComponent } from '../boleta-card/boleta-card';

type Pestana = 'vigentes' | 'pasadas';

@Component({
  selector: 'app-boletas-panel',
  imports: [BoletaCardComponent],
  templateUrl: './boletas-panel.html',
  styleUrl: './boletas-panel.css',
})
export class BoletasPanelComponent implements OnInit {
  private authService = inject(AuthService);
  private boletaService = inject(BoletaService);

  readonly abrirFestivales = output<void>();

  protected cargando = signal(true);
  protected error = signal(false);
  protected pestana = signal<Pestana>('vigentes');

  private boletas = signal<BoletaDetalle[]>([]);

  protected vigentes = computed(() =>
    this.boletas()
      .filter((boleta) => estadoFestival(boleta.festival) !== 'finalizado')
      .sort((a, b) => a.festival.fechaInicio.localeCompare(b.festival.fechaInicio)),
  );

  protected pasadas = computed(() =>
    this.boletas()
      .filter((boleta) => estadoFestival(boleta.festival) === 'finalizado')
      .sort((a, b) => b.festival.fechaInicio.localeCompare(a.festival.fechaInicio)),
  );

  protected visibles = computed(() =>
    this.pestana() === 'vigentes' ? this.vigentes() : this.pasadas(),
  );

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set(false);

    this.boletaService
      .listarDetallePorComprador(this.authService.sesion()?.documento ?? 0)
      .subscribe({
        next: (boletas) => {
          this.boletas.set(boletas);
          this.cargando.set(false);
        },
        error: () => {
          this.error.set(true);
          this.cargando.set(false);
        },
      });
  }
}
