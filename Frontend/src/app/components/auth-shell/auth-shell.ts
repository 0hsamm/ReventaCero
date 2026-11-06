import { Component, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { ThemeService } from '../../services/theme.service';

@Component({
  selector: 'app-auth-shell',
  imports: [RouterLink],
  templateUrl: './auth-shell.html',
  styleUrl: './auth-shell.css',
})
export class AuthShellComponent {
  protected themeService = inject(ThemeService);

  readonly etiqueta = input('');
  readonly lineaNeon = input.required<string>();
  readonly lineaHueca = input('');
  readonly grafiti = input('');
  readonly texto = input('');
  readonly paso = input('');
  readonly tituloTarjeta = input('');
  readonly ancha = input(false);
}
