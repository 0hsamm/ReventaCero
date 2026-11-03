import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, forkJoin, map } from 'rxjs';

import { environment } from '../../environment/environment';
import {
  Artista,
  DetalleFestival,
  Festival,
  Presentacion,
  TipoEntrada,
  Zona,
} from '../models/festival';

// Rutas previstas: aún no hay controladores para estos recursos en el backend.
@Injectable({
  providedIn: 'root',
})
export class FestivalService {
  private http = inject(HttpClient);

  private apiUrl = environment.apiUrl;

  listar(): Observable<Festival[]> {
    return this.http.get<Festival[]>(`${this.apiUrl}/festivales`);
  }

  listarZonas(): Observable<Zona[]> {
    return this.http.get<Zona[]>(`${this.apiUrl}/zonas`);
  }

  listarTiposEntrada(): Observable<TipoEntrada[]> {
    return this.http.get<TipoEntrada[]>(`${this.apiUrl}/tipos-entrada`);
  }

  obtenerDetalle(idFestival: number): Observable<DetalleFestival> {
    return forkJoin({
      zonas: this.listarZonas(),
      tiposEntrada: this.listarTiposEntrada(),
      presentaciones: this.listarPresentaciones(idFestival),
      artistas: this.listarArtistas(),
    }).pipe(
      map(({ zonas, tiposEntrada, presentaciones, artistas }) => {
        const zonasFestival = zonas.filter((zona) => zona.idFestival === idFestival);
        const idsZona = new Set(zonasFestival.map((zona) => zona.idZona));

        return {
          zonas: zonasFestival,
          tiposEntrada: tiposEntrada.filter((tipo) => idsZona.has(tipo.idZona)),
          presentaciones,
          artistas,
        };
      }),
    );
  }

  private listarPresentaciones(idFestival: number): Observable<Presentacion[]> {
    return this.http.get<Presentacion[]>(`${this.apiUrl}/presentaciones/festival/${idFestival}`);
  }

  private listarArtistas(): Observable<Artista[]> {
    return this.http.get<Artista[]>(`${this.apiUrl}/artistas`);
  }
}
