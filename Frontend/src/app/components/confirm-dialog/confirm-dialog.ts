import { Component, ElementRef, effect, input, output, viewChild } from '@angular/core';

@Component({
  selector: 'app-confirm-dialog',
  templateUrl: './confirm-dialog.html',
  styleUrl: './confirm-dialog.css',
})
export class ConfirmDialogComponent {
  readonly visible = input(false);
  readonly title = input('Confirmar');
  readonly message = input('');
  readonly confirmLabel = input('Aceptar');
  readonly cancelLabel = input('Cancelar');

  readonly confirmed = output<void>();
  readonly cancelled = output<void>();

  private dialogo = viewChild.required<ElementRef<HTMLDialogElement>>('dialogo');

  constructor() {
    effect(() => {
      const dialogo = this.dialogo().nativeElement;

      if (this.visible() && !dialogo.open) {
        dialogo.showModal();
      } else if (!this.visible() && dialogo.open) {
        dialogo.close();
      }
    });
  }

  // Esc cierra el diálogo nativo: se avisa al padre para que su estado no quede desfasado.
  protected alCancelar(evento: Event): void {
    evento.preventDefault();
    this.cancelled.emit();
  }
}
