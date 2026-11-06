import { Component, computed, input, model } from '@angular/core';

export const TAMANO_PAGINA = 10;

@Component({
  selector: 'app-paginacion',
  template: `
    @if (paginas() > 1) {
      <nav class="paginacion" aria-label="Paginación">
        <button
          type="button"
          class="btn btn-ghost btn-sm"
          [disabled]="pagina() === 1"
          (click)="pagina.set(pagina() - 1)"
        >
          Anterior
        </button>
        <span>Página {{ pagina() }} de {{ paginas() }}</span>
        <button
          type="button"
          class="btn btn-ghost btn-sm"
          [disabled]="pagina() === paginas()"
          (click)="pagina.set(pagina() + 1)"
        >
          Siguiente
        </button>
      </nav>
    }
  `,
  styles: `
    .paginacion {
      display: flex;
      align-items: center;
      justify-content: flex-end;
      gap: 12px;
      margin-top: 12px;
      color: var(--text-2);
      font-size: 14px;
    }
  `,
})
export class PaginacionComponent {
  readonly total = input.required<number>();
  readonly pagina = model(1);

  protected paginas = computed(() => Math.max(1, Math.ceil(this.total() / TAMANO_PAGINA)));
}
