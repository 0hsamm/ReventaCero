import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { SidebarComponent } from '../../sidebar/sidebar';

@Component({
  selector: 'app-main-layout',
  imports: [RouterOutlet, SidebarComponent],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.css',
})
// Angular requiere la clase; este componente solo compone la plantilla y sus imports.
// skipcq: JS-0327
export class MainLayoutComponent {}
