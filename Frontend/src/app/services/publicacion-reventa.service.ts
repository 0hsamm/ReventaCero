import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, switchMap, tap } from 'rxjs';

import { environment } from '../../environment/environment';
import { PublicacionReventa } from '../models/modelos';
import { BoletaService } from './boleta.service';

// Boletas publicadas para que cualquier usuario las compre. Siguen siendo del revendedor hasta la venta.
@Injectable({
  providedIn: 'root',
})
export class PublicacionReventaService {
  private http = inject(HttpClient);
  private boletaService = inject(BoletaService);

  private api = `${environment.apiUrl}/publicaciones-reventa`;

  /** Ventas del revendedor: el menú lateral las suma a su contador de notificaciones. */
  readonly vendidas = signal(0);

  disponibles(): Observable<PublicacionReventa[]> {
    return this.http.get<PublicacionReventa[]>(this.api);
  }

  mias(): Observable<PublicacionReventa[]> {
    return this.http
      .get<PublicacionReventa[]>(`${this.api}/mias`)
      .pipe(tap((lista) => this.vendidas.set(lista.filter((p) => p.estado === 'VENDIDA').length)));
  }

  publicar(codigoBoleta: number, precio: number): Observable<PublicacionReventa> {
    return this.http.post<PublicacionReventa>(this.api, { codigoBoleta, precio });
  }

  comprar(id: number, metodoPago: string): Observable<PublicacionReventa> {
    return this.boletaService
      .dispositivoActivo()
      .pipe(
        switchMap((dispositivo) =>
          this.http.put<PublicacionReventa>(`${this.api}/${id}/comprar`, {
            idDispositivo: dispositivo.idDispositivo,
            metodoPago,
          }),
        ),
      );
  }

  retirar(id: number): Observable<PublicacionReventa> {
    return this.http.put<PublicacionReventa>(`${this.api}/${id}/retirar`, {});
  }
}
