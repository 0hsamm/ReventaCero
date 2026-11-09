import { Component, computed, input, signal } from '@angular/core';

export interface DatoGrafica {
  etiqueta: string;
  valor: number;
  texto: string;
}

// Una sola serie: el título la nombra, no necesita leyenda. Cada barra muestra su valor al pasar
// el cursor o enfocarla, y la vista de tabla da los mismos datos sin depender del gráfico.
@Component({
  selector: 'app-grafica-barras',
  templateUrl: './grafica-barras.html',
  styleUrl: './grafica-barras.css',
})
export class GraficaBarrasComponent {
  readonly titulo = input.required<string>();
  readonly datos = input.required<DatoGrafica[]>();
  readonly orientacion = input<'columnas' | 'horizontal'>('columnas');

  protected verTabla = signal(false);

  protected maximo = computed(() => Math.max(1, ...this.datos().map((dato) => dato.valor)));
}
