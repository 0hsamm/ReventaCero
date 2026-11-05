import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, switchMap, tap } from 'rxjs';

import { environment } from '../../environment/environment';
import { SolicitudReventa } from '../models/modelos';
import { BoletaService } from './boleta.service';

// La boleta solo cambia de dueño cuando quien la recibe acepta la oferta.
@Injectable({
  providedIn: 'root',
})
export class SolicitudReventaService {
  private http = inject(HttpClient);
  private boletaService = inject(BoletaService);

  private api = `${environment.apiUrl}/solicitudes-reventa`;

  /** Ofertas sin responder: el menú lateral las suma a su contador de notificaciones. */
  readonly pendientes = signal(0);

  ofrecer(
    codigoBoleta: number,
    compradorDocumento: number,
    precio: number,
    metodoPago: string,
  ): Observable<SolicitudReventa> {
    return this.http.post<SolicitudReventa>(this.api, {
      codigoBoleta,
      compradorDocumento,
      precio,
      metodoPago,
    });
  }

  recibidas(): Observable<SolicitudReventa[]> {
    return this.http
      .get<SolicitudReventa[]>(`${this.api}/recibidas`)
      .pipe(tap((lista) => this.pendientes.set(lista.filter((s) => s.estado === 'PENDIENTE').length)));
  }

  enviadas(): Observable<SolicitudReventa[]> {
    return this.http.get<SolicitudReventa[]>(`${this.api}/enviadas`);
  }

  aceptar(id: number): Observable<SolicitudReventa> {
    return this.boletaService
      .dispositivoActivo()
      .pipe(
        switchMap((dispositivo) =>
          this.http.put<SolicitudReventa>(`${this.api}/${id}/aceptar`, {
            idDispositivo: dispositivo.idDispositivo,
          }),
        ),
      );
  }

  rechazar(id: number): Observable<SolicitudReventa> {
    return this.http.put<SolicitudReventa>(`${this.api}/${id}/rechazar`, {});
  }

  cancelar(id: number): Observable<SolicitudReventa> {
    return this.http.put<SolicitudReventa>(`${this.api}/${id}/cancelar`, {});
  }
}
