import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, switchMap, tap } from 'rxjs';

import { environment } from '../../environment/environment';
import { PublicacionReventa, ReventaAutorizada } from '../models/modelos';
import { BoletaService } from './boleta.service';

// Boletas publicadas para que cualquier usuario las compre. Siguen siendo del revendedor hasta la venta.
@Injectable({
  providedIn: 'root',
})
export class PublicacionReventaService {
  private http = inject(HttpClient);
  private boletaService = inject(BoletaService);

  private api = `${environment.apiUrl}/publicaciones-reventa`;

  /** Boletas vendidas: el menú lateral las suma a su contador de notificaciones. */
  readonly vendidas = signal(0);

  disponibles(): Observable<PublicacionReventa[]> {
    return this.http.get<PublicacionReventa[]>(this.api);
  }

  mias(): Observable<PublicacionReventa[]> {
    return this.http.get<PublicacionReventa[]>(`${this.api}/mias`);
  }

  /** Las ventas ya quedan en las reventas registradas: de ahí sale el aviso al revendedor. */
  ventas(documento: number): Observable<ReventaAutorizada[]> {
    return this.boletaService
      .reventasComoVendedor(documento)
      .pipe(tap((lista) => this.vendidas.set(lista.length)));
  }

  publicar(codigoBoleta: number, precio: number): Observable<unknown> {
    return this.http.post(this.api, { codigoBoleta, precio });
  }

  comprar(referencia: number, metodoPago: string): Observable<unknown> {
    return this.boletaService
      .dispositivoActivo()
      .pipe(
        switchMap((dispositivo) =>
          this.http.put(`${this.api}/${referencia}/comprar`, {
            idDispositivo: dispositivo.idDispositivo,
            metodoPago,
          }),
        ),
      );
  }

  retirar(codigoBoleta: number): Observable<unknown> {
    return this.http.put(`${this.api}/boleta/${codigoBoleta}/retirar`, {});
  }
}
