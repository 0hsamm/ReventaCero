import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  ElementRef,
  OnDestroy,
  effect,
  inject,
  input,
  signal,
  viewChild,
} from '@angular/core';

import { BoletaService } from '../../services/boleta.service';
import { mensajeError } from '../../utils/validaciones';

@Component({
  selector: 'app-qr-boleta',
  templateUrl: './qr-boleta.html',
  styleUrl: './qr-boleta.css',
})
export class QrBoletaComponent implements OnDestroy {
  private boletaService = inject(BoletaService);

  readonly codigo = input.required<number>();

  protected url = signal<string | null>(null);
  protected error = signal(false);
  protected texto = signal('');
  protected errorTexto = signal('');

  private dialogo = viewChild<ElementRef<HTMLDialogElement>>('dialogo');

  constructor() {
    effect((onCleanup) => {
      const codigo = this.codigo();
      this.error.set(false);
      this.texto.set('');

      const suscripcion = this.boletaService.qr(codigo).subscribe({
        next: (svg) => this.cambiarUrl(URL.createObjectURL(svg)),
        error: () => this.error.set(true),
      });

      onCleanup(() => suscripcion.unsubscribe());
    });
  }

  protected ampliar(): void {
    this.dialogo()?.nativeElement.showModal();

    if (this.texto() === '') {
      this.errorTexto.set('');
      this.boletaService.qrTexto(this.codigo()).subscribe({
        next: (texto) => this.texto.set(texto),
        error: (err: HttpErrorResponse) =>
          this.errorTexto.set(mensajeError(err, 'No pudimos leer el contenido del QR')),
      });
    }
  }

  protected cerrar(): void {
    this.dialogo()?.nativeElement.close();
  }

  protected clicEnDialogo(evento: MouseEvent): void {
    if (evento.target === this.dialogo()?.nativeElement) {
      this.cerrar();
    }
  }

  ngOnDestroy(): void {
    this.cambiarUrl(null);
  }

  private cambiarUrl(nueva: string | null): void {
    const anterior = this.url();

    if (anterior) {
      URL.revokeObjectURL(anterior);
    }

    this.url.set(nueva);
  }
}
