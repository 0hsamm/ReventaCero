import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environment/environment';
import {
  Boleta,
  ControlAcceso,
  Devolucion,
  Organizador,
  ReventaAutorizada,
  Transaccion,
  Usuario,
} from '../models/modelos';

@Injectable({
  providedIn: 'root',
})
export class AdminService {
  private http = inject(HttpClient);

  private api = environment.apiUrl;

  usuarios(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${this.api}/usuarios`);
  }

  cambiarEstadoUsuario(documento: number, estado: string): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.api}/usuarios/${documento}/estado`, { estado });
  }

  organizadores(): Observable<Organizador[]> {
    return this.http.get<Organizador[]>(`${this.api}/organizadores`);
  }

  crearOrganizador(organizador: Partial<Organizador>): Observable<Organizador> {
    return this.http.post<Organizador>(`${this.api}/organizadores`, organizador);
  }

  boletas(): Observable<Boleta[]> {
    return this.http.get<Boleta[]>(`${this.api}/boletas`);
  }

  transacciones(): Observable<Transaccion[]> {
    return this.http.get<Transaccion[]>(`${this.api}/transacciones`);
  }

  reventas(): Observable<ReventaAutorizada[]> {
    return this.http.get<ReventaAutorizada[]>(`${this.api}/reventas-autorizadas`);
  }

  devoluciones(): Observable<Devolucion[]> {
    return this.http.get<Devolucion[]>(`${this.api}/devoluciones`);
  }

  controlesAcceso(): Observable<ControlAcceso[]> {
    return this.http.get<ControlAcceso[]>(`${this.api}/controles-acceso`);
  }

  accesosDeBoleta(codigoBoleta: number): Observable<ControlAcceso[]> {
    return this.http.get<ControlAcceso[]>(`${this.api}/controles-acceso/boleta/${codigoBoleta}`);
  }

  /** Un ingreso denegado llega como 403 con el mismo DTO: lo resuelve quien se suscribe. */
  registrarIngreso(codigoBoleta: number): Observable<ControlAcceso> {
    return this.http.post<ControlAcceso>(`${this.api}/controles-acceso/boleta/${codigoBoleta}`, {});
  }
}
