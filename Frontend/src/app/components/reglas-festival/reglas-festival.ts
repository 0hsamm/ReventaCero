import { DatePipe } from '@angular/common';
import { Component, input } from '@angular/core';

export const MAXIMO_POR_COMPRA = 6;

export interface ItemCartel {
  idPresentacion: number;
  artista: string;
  horaInicio: string;
  nombreEscenario: string;
}

interface Regla {
  titulo: string;
  texto: string;
}

const REGLAS: Regla[] = [
  {
    titulo: 'Máximo por compra',
    texto: `Puedes comprar hasta ${MAXIMO_POR_COMPRA} boletas en una misma orden. Cada boleta queda a tu nombre.`,
  },
  {
    titulo: 'Ingreso con código QR',
    texto:
      'Cada boleta tiene un QR único con un código de verificación. Lo encuentras en Mis boletas; preséntalo junto con tu documento en la entrada. Si la revendes, el QR anterior deja de servir.',
  },
  {
    titulo: 'Reventa con precio tope',
    texto:
      'Solo puedes revender dentro de ReventaCero y nunca por encima del tope que fijó el organizador. Las boletas en reventa aparecen en la pestaña "Boletas en Reventa" de cada festival.',
  },
  {
    titulo: 'Devoluciones',
    texto:
      'Puedes devolver una boleta antes de que empiece el festival y se te reembolsa el valor pagado. Una boleta devuelta deja de ser válida.',
  },
  {
    titulo: 'Zonas agotadas',
    texto:
      'Si una zona se agota, únete a su lista de espera: te avisamos en Notificaciones cuando se liberen cupos.',
  },
  {
    titulo: 'Cuenta verificada',
    texto:
      'Para comprar necesitas una cuenta de usuario con el correo verificado. Así las boletas llegan a personas reales.',
  },
];

@Component({
  selector: 'app-reglas-festival',
  imports: [DatePipe],
  templateUrl: './reglas-festival.html',
  styleUrl: './reglas-festival.css',
})
export class ReglasFestivalComponent {
  readonly imagen = input.required<string>();
  readonly conSesion = input(false);
  readonly cartel = input<ItemCartel[]>([]);

  protected readonly reglas = REGLAS;
}
