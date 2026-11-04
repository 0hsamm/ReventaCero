import { Component, input } from '@angular/core';

// Etiqueta, control proyectado y mensaje de error de un campo de formulario.
@Component({
  selector: 'app-campo',
  template: `
    <div class="field">
      <label [attr.for]="para()">{{ etiqueta() }}</label>
      <ng-content></ng-content>
      @if (error()) {
        <span class="field-error" role="alert">{{ error() }}</span>
      } @else if (ayuda()) {
        <span class="field-help">{{ ayuda() }}</span>
      }
    </div>
  `,
})
export class CampoComponent {
  readonly etiqueta = input.required<string>();
  readonly para = input.required<string>();
  readonly error = input<string | undefined>('');
  readonly ayuda = input('');
}
