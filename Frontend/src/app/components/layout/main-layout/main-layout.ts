import { Component, signal } from '@angular/core';

import { BoletasPanelComponent } from '../../boletas-panel/boletas-panel';
import { FestivalesPanelComponent } from '../../festivales-panel/festivales-panel';
import { InicioPanelComponent } from '../../inicio-panel/inicio-panel';
import { Panel, SidebarComponent } from '../../sidebar/sidebar';

@Component({
  selector: 'app-main-layout',
  imports: [
    SidebarComponent,
    InicioPanelComponent,
    FestivalesPanelComponent,
    BoletasPanelComponent,
  ],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.css',
})
export class MainLayoutComponent {
  protected panelActivo = signal<Panel>('inicio');

  protected festivalSeleccionado = signal<number | null>(null);

  protected cambiarPanel(panel: Panel): void {
    this.festivalSeleccionado.set(null);
    this.panelActivo.set(panel);
  }

  protected abrirFestival(idFestival: number): void {
    this.festivalSeleccionado.set(idFestival);
    this.panelActivo.set('festivales');
  }
}
