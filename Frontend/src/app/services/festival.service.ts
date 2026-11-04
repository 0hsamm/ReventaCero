import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environment/environment';
import {
  Artista,
  Escenario,
  Festival,
  Genero,
  Presentacion,
  TipoEntrada,
  Zona,
} from '../models/modelos';

// Solo GET /festivales es público: lo demás exige haber iniciado sesión.
@Injectable({
  providedIn: 'root',
})
export class FestivalService {
  private http = inject(HttpClient);

  private api = environment.apiUrl;

  listar(): Observable<Festival[]> {
    return this.http.get<Festival[]>(`${this.api}/festivales`);
  }

  buscar(id: number): Observable<Festival> {
    return this.http.get<Festival>(`${this.api}/festivales/${id}`);
  }

  listarPorOrganizador(documento: number): Observable<Festival[]> {
    return this.http.get<Festival[]>(`${this.api}/festivales/organizador/${documento}`);
  }

  crear(festival: Partial<Festival>): Observable<Festival> {
    return this.http.post<Festival>(`${this.api}/festivales`, festival);
  }

  actualizar(id: number, festival: Partial<Festival>): Observable<Festival> {
    return this.http.put<Festival>(`${this.api}/festivales/${id}`, festival);
  }

  eliminar(id: number): Observable<unknown> {
    return this.http.delete(`${this.api}/festivales/${id}`, { responseType: 'text' });
  }

  listarZonas(): Observable<Zona[]> {
    return this.http.get<Zona[]>(`${this.api}/zonas`);
  }

  zonasDeFestival(id: number): Observable<Zona[]> {
    return this.http.get<Zona[]>(`${this.api}/zonas/festival/${id}`);
  }

  aforoDisponible(idZona: number): Observable<number> {
    return this.http.get<number>(`${this.api}/zonas/${idZona}/aforo-disponible`);
  }

  crearZona(zona: Partial<Zona>): Observable<Zona> {
    return this.http.post<Zona>(`${this.api}/zonas`, zona);
  }

  actualizarZona(id: number, zona: Partial<Zona>): Observable<Zona> {
    return this.http.put<Zona>(`${this.api}/zonas/${id}`, zona);
  }

  eliminarZona(id: number): Observable<unknown> {
    return this.http.delete(`${this.api}/zonas/${id}`, { responseType: 'text' });
  }

  listarTipos(): Observable<TipoEntrada[]> {
    return this.http.get<TipoEntrada[]>(`${this.api}/tipos-entrada`);
  }

  crearTipo(tipo: Partial<TipoEntrada>): Observable<TipoEntrada> {
    return this.http.post<TipoEntrada>(`${this.api}/tipos-entrada`, tipo);
  }

  actualizarTipo(id: number, tipo: Partial<TipoEntrada>): Observable<TipoEntrada> {
    return this.http.put<TipoEntrada>(`${this.api}/tipos-entrada/${id}`, tipo);
  }

  eliminarTipo(id: number): Observable<unknown> {
    return this.http.delete(`${this.api}/tipos-entrada/${id}`, { responseType: 'text' });
  }

  escenariosDeFestival(id: number): Observable<Escenario[]> {
    return this.http.get<Escenario[]>(`${this.api}/escenarios/festival/${id}`);
  }

  crearEscenario(escenario: Escenario): Observable<Escenario> {
    return this.http.post<Escenario>(`${this.api}/escenarios`, escenario);
  }

  eliminarEscenario(id: number, nombre: string): Observable<unknown> {
    return this.http.delete(`${this.api}/escenarios/${id}/${encodeURIComponent(nombre)}`, {
      responseType: 'text',
    });
  }

  presentacionesDeFestival(id: number): Observable<Presentacion[]> {
    return this.http.get<Presentacion[]>(`${this.api}/presentaciones/festival/${id}`);
  }

  crearPresentacion(presentacion: Partial<Presentacion>): Observable<Presentacion> {
    return this.http.post<Presentacion>(`${this.api}/presentaciones`, presentacion);
  }

  eliminarPresentacion(id: number): Observable<unknown> {
    return this.http.delete(`${this.api}/presentaciones/${id}`, { responseType: 'text' });
  }

  listarArtistas(): Observable<Artista[]> {
    return this.http.get<Artista[]>(`${this.api}/artistas`);
  }

  crearArtista(artista: Partial<Artista>): Observable<Artista> {
    return this.http.post<Artista>(`${this.api}/artistas`, artista);
  }

  actualizarArtista(id: number, artista: Partial<Artista>): Observable<Artista> {
    return this.http.put<Artista>(`${this.api}/artistas/${id}`, artista);
  }

  eliminarArtista(id: number): Observable<unknown> {
    return this.http.delete(`${this.api}/artistas/${id}`, { responseType: 'text' });
  }

  listarGeneros(): Observable<Genero[]> {
    return this.http.get<Genero[]>(`${this.api}/generos`);
  }

  crearGenero(nombre: string): Observable<Genero> {
    return this.http.post<Genero>(`${this.api}/generos`, { nombre });
  }
}
