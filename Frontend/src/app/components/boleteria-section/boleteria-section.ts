import { CurrencyPipe } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

import { EstadoFase, FASES } from '../../data/contenido-landing';

@Component({
  selector: 'app-boleteria-section',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './boleteria-section.html',
  styleUrl: './boleteria-section.css',
})
export class BoleteriaSectionComponent {
  protected readonly fases = FASES;

  protected readonly etiquetaEstado: Record<EstadoFase, string> = {
    agotada: 'Agotada',
    activa: 'A la venta',
    proxima: 'Disponible',
  };
}
