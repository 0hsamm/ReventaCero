import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

const TITULOS: Record<string, string> = {
  terminos: 'Términos y condiciones',
  privacidad: 'Tratamiento de datos',
  devoluciones: 'Devoluciones y retracto',
  reventa: 'Política de reventa',
};

@Component({
  selector: 'app-legal',
  imports: [RouterLink],
  templateUrl: './legal.html',
  styleUrl: './legal.css',
})
export class LegalComponent {
  protected readonly titulo =
    TITULOS[inject(ActivatedRoute).snapshot.paramMap.get('documento') ?? ''] ?? 'Documento legal';
}
