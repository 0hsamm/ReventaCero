import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { environment } from '../../environment/environment';
import {
  INICIO_ROL,
  LoginDTO,
  RegistroDTO,
  RespuestaAutenticacionDTO,
  Sesion,
  TipoUsuario,
} from '../models/modelos';

const CLAVES_SESION = ['token', 'tipoToken', 'documento', 'nombreUsuario', 'correo', 'tipoUsuario'];

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);
  private readonly almacenamiento = localStorage;

  private apiUrl = `${environment.apiUrl}/auth`;

  readonly sesion = signal<Sesion | null>(this.leerSesion());

  login(datos: LoginDTO): Observable<RespuestaAutenticacionDTO> {
    return this.http
      .post<RespuestaAutenticacionDTO>(`${this.apiUrl}/login`, datos)
      .pipe(tap((respuesta) => this.guardarSesion(respuesta)));
  }

  registro(datos: RegistroDTO): Observable<string> {
    return this.http.post(`${this.apiUrl}/registro`, datos, { responseType: 'text' });
  }

  obtenerToken(): string | null {
    return this.almacenamiento.getItem('token');
  }

  rutaInicio(): string {
    return INICIO_ROL[this.sesion()?.tipoUsuario ?? 'USUARIO'];
  }

  cerrarSesion(): void {
    CLAVES_SESION.forEach((clave) => this.almacenamiento.removeItem(clave));
    this.sesion.set(null);
  }

  private guardarSesion(respuesta: RespuestaAutenticacionDTO): void {
    this.almacenamiento.setItem('token', respuesta.token);
    this.almacenamiento.setItem('tipoToken', respuesta.tipoToken);
    this.almacenamiento.setItem('documento', String(respuesta.documento));
    this.almacenamiento.setItem('nombreUsuario', respuesta.nombreUsuario);
    this.almacenamiento.setItem('correo', respuesta.correo);
    this.almacenamiento.setItem('tipoUsuario', respuesta.tipoUsuario ?? '');

    this.sesion.set(this.leerSesion());
  }

  private leerSesion(): Sesion | null {
    const token = this.almacenamiento.getItem('token');

    if (token == null || AuthService.tokenVencido(token)) {
      CLAVES_SESION.forEach((clave) => this.almacenamiento.removeItem(clave));
      return null;
    }

    return {
      documento: Number(this.almacenamiento.getItem('documento')),
      nombreUsuario: this.almacenamiento.getItem('nombreUsuario') ?? '',
      correo: this.almacenamiento.getItem('correo') ?? '',
      tipoUsuario: (this.almacenamiento.getItem('tipoUsuario') || 'USUARIO') as TipoUsuario,
    };
  }

  private static tokenVencido(token: string): boolean {
    try {
      const datos = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));

      return typeof datos.exp === 'number' && datos.exp * 1000 < Date.now();
    } catch {
      return true;
    }
  }
}
