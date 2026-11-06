import { DatePipe } from '@angular/common';
import { Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Festival, cartelFestival } from '../../models/modelos';

@Component({
  selector: 'app-evento-card',
  imports: [DatePipe, RouterLink],
  templateUrl: './evento-card.html',
  styleUrl: './evento-card.css',
})
export class EventoCardComponent {
  readonly festival = input.required<Festival>();

  protected imagen = computed(() => cartelFestival(this.festival().idFestival));
}
