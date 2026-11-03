import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, input } from '@angular/core';

import { BoletaDetalle } from '../../models/boleta';

@Component({
  selector: 'app-boleta-card',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './boleta-card.html',
  styleUrl: './boleta-card.css',
})
export class BoletaCardComponent {
  readonly boleta = input.required<BoletaDetalle>();
}
