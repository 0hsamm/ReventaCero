import { CurrencyPipe } from '@angular/common';
import { Component, ElementRef, input, output, viewChild } from '@angular/core';

export interface BoletaResumen {
  nombre: string;
  precio: number;
}

@Component({
  selector: 'app-dialogo-sesion',
  imports: [CurrencyPipe],
  templateUrl: './dialogo-sesion.html',
  styleUrl: './dialogo-sesion.css',
})
export class DialogoSesionComponent {
  readonly boleta = input<BoletaResumen | null>(null);

  readonly ingresar = output();
  readonly registrarse = output();

  private dialogo = viewChild.required<ElementRef<HTMLDialogElement>>('dialogo');

  abrir(): void {
    this.dialogo().nativeElement.showModal();
  }

  cerrar(): void {
    this.dialogo().nativeElement.close();
  }

  protected clicFuera(evento: MouseEvent): void {
    if (evento.target === this.dialogo().nativeElement) {
      this.cerrar();
    }
  }

  protected elegirIngresar(): void {
    this.cerrar();
    this.ingresar.emit();
  }

  protected elegirRegistro(): void {
    this.cerrar();
    this.registrarse.emit();
  }
}
