import { Component } from '@angular/core';

import { ARTISTAS } from '../../data/contenido-landing';

@Component({
  selector: 'app-lineup-section',
  templateUrl: './lineup-section.html',
  styleUrl: './lineup-section.css',
})
export class LineupSectionComponent {
  protected readonly artistas = ARTISTAS;
}
