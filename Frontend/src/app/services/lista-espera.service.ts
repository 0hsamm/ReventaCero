import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environment/environment';
import { Inscripcion, Notificacion } from '../models/modelos';

@Injectable({
  providedIn: 'root',
})
export class ListaEsperaService {
  private http = inject(HttpClient);

  private api = environment.apiUrl;

  inscribir(documento: number, idTipoEntrada: number): Observable<Inscripcion> {
    return this.http.post<Inscripcion>(`${this.api}/inscripciones`, { documento, idTipoEntrada });
  }

  inscripciones(documento: number): Observable<Inscripcion[]> {
    return this.http.get<Inscripcion[]>(`${this.api}/inscripciones/usuario/${documento}`);
  }

  posicion(documento: number, idLista: number): Observable<number> {
    return this.http.get<number>(`${this.api}/inscripciones/${documento}/${idLista}/posicion`);
  }

  cancelar(documento: number, idLista: number): Observable<unknown> {
    return this.http.delete(`${this.api}/inscripciones/${documento}/${idLista}`, {
      responseType: 'text',
    });
  }

  notificaciones(documento: number): Observable<Notificacion[]> {
    return this.http.get<Notificacion[]>(`${this.api}/notificaciones/usuario/${documento}`);
  }
}
