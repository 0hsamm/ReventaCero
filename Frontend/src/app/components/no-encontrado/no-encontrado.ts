import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-no-encontrado',
  imports: [RouterLink],
  template: `
    <main class="pagina-panel">
      <h1 class="titulo-seccion">Error <span class="contorno">404</span></h1>
      <p class="nota">Esta página no existe.</p>
      <a class="btn btn-primary" routerLink="/">Volver al inicio</a>
    </main>
  `,
  styles: `
    main {
      min-height: 60vh;
      padding-block: 64px;
    }

    p {
      margin: 16px 0;
      font-size: 18px;
    }
  `,
})
export class NoEncontradoComponent {}
