import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-site-footer',
  imports: [RouterLink],
  templateUrl: './site-footer.html',
  styleUrl: './site-footer.css',
})
export class SiteFooterComponent {
  protected readonly anio = new Date().getFullYear();

  protected readonly legales = [
    { ruta: 'terminos', etiqueta: 'Términos y condiciones' },
    { ruta: 'privacidad', etiqueta: 'Tratamiento de datos' },
    { ruta: 'devoluciones', etiqueta: 'Devoluciones y retracto' },
    { ruta: 'reventa', etiqueta: 'Política de reventa' },
  ];
}
