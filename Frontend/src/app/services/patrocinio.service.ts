import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environment/environment';
import { Patrocinador, Patrocinio } from '../models/modelos';

@Injectable({
  providedIn: 'root',
})
export class PatrocinioService {
  private http = inject(HttpClient);

  private api = environment.apiUrl;

  listarPatrocinadores(): Observable<Patrocinador[]> {
    return this.http.get<Patrocinador[]>(`${this.api}/patrocinadores`);
  }

  crearPatrocinador(patrocinador: Partial<Patrocinador>): Observable<Patrocinador> {
    return this.http.post<Patrocinador>(`${this.api}/patrocinadores`, patrocinador);
  }

  actualizarPatrocinador(id: number, patrocinador: Partial<Patrocinador>): Observable<Patrocinador> {
    return this.http.put<Patrocinador>(`${this.api}/patrocinadores/${id}`, patrocinador);
  }

  eliminarPatrocinador(id: number): Observable<unknown> {
    return this.http.delete(`${this.api}/patrocinadores/${id}`, { responseType: 'text' });
  }

  patrociniosDeFestival(idFestival: number): Observable<Patrocinio[]> {
    return this.http.get<Patrocinio[]>(`${this.api}/patrocinios/festival/${idFestival}`);
  }

  totalDeFestival(idFestival: number): Observable<number> {
    return this.http.get<number>(`${this.api}/patrocinios/festival/${idFestival}/total`);
  }

  crearPatrocinio(patrocinio: Partial<Patrocinio>): Observable<Patrocinio> {
    return this.http.post<Patrocinio>(`${this.api}/patrocinios`, patrocinio);
  }

  actualizarPatrocinio(id: number, patrocinio: Partial<Patrocinio>): Observable<Patrocinio> {
    return this.http.put<Patrocinio>(`${this.api}/patrocinios/${id}`, patrocinio);
  }

  eliminarPatrocinio(id: number): Observable<unknown> {
    return this.http.delete(`${this.api}/patrocinios/${id}`, { responseType: 'text' });
  }
}
