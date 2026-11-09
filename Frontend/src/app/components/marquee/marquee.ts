import { Component, input, signal } from '@angular/core';

@Component({
  selector: 'app-marquee',
  templateUrl: './marquee.html',
  styleUrl: './marquee.css',
})
export class MarqueeComponent {
  readonly texto = input.required<string>();
  readonly variante = input<'neon' | 'contorno'>('neon');

  protected pausado = signal(false);
}
