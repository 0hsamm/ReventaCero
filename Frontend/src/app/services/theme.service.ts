import { Injectable, signal } from '@angular/core';

export type Tema = 'claro' | 'oscuro';

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  private readonly storageKey = 'tema';

  readonly tema = signal<Tema>(
    localStorage.getItem(this.storageKey) === 'claro' ? 'claro' : 'oscuro',
  );

  inicializar(): void {
    ThemeService.aplicar(this.tema());
  }

  alternar(): void {
    const tema: Tema = this.tema() === 'claro' ? 'oscuro' : 'claro';

    localStorage.setItem(this.storageKey, tema);
    this.tema.set(tema);
    ThemeService.aplicar(tema);
  }

  private static aplicar(tema: Tema): void {
    document.documentElement.setAttribute('data-theme', tema === 'claro' ? 'light' : 'dark');
  }
}
