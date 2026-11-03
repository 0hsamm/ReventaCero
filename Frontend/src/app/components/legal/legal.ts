import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { SiteFooterComponent } from '../site-footer/site-footer';

const TITULOS: Record<string, string> = {
  terminos: 'Términos y condiciones',
  privacidad: 'Tratamiento de datos',
  devoluciones: 'Devoluciones y retracto',
  reventa: 'Política de reventa',
};

@Component({
  selector: 'app-legal',
  imports: [RouterLink, SiteFooterComponent],
  templateUrl: './legal.html',
  styleUrl: './legal.css',
})
export class LegalComponent {
  protected readonly titulo =
    TITULOS[inject(ActivatedRoute).snapshot.paramMap.get('documento') ?? ''] ?? 'Documento legal';
}
