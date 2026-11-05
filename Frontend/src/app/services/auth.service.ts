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
} from '../models/modelos';
import { sesionDesdeToken } from '../utils/sesion';

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
    const sesion = this.sesion();

    return sesion ? INICIO_ROL[sesion.tipoUsuario] : '/acceso';
  }

  validarSesion(): Sesion | null {
    const sesion = this.leerSesion();

    this.sesion.set(sesion);
    return sesion;
  }

  cerrarSesion(): void {
    CLAVES_SESION.forEach((clave) => this.almacenamiento.removeItem(clave));
    this.sesion.set(null);
  }

  private guardarSesion(respuesta: RespuestaAutenticacionDTO): void {
    const sesion = sesionDesdeToken(respuesta.token);

    if (sesion === null) {
      this.cerrarSesion();
      throw new Error('La sesión recibida no tiene un rol válido o ha vencido. Vuelve a ingresar.');
    }

    this.almacenamiento.setItem('token', respuesta.token);
    this.almacenamiento.setItem('tipoToken', respuesta.tipoToken);
    this.guardarDatosSesion(sesion);

    this.sesion.set(sesion);
  }

  private guardarDatosSesion(sesion: Sesion): void {
    this.almacenamiento.setItem('documento', String(sesion.documento));
    this.almacenamiento.setItem('nombreUsuario', sesion.nombreUsuario);
    this.almacenamiento.setItem('correo', sesion.correo);
    this.almacenamiento.setItem('tipoUsuario', sesion.tipoUsuario);
  }

  private leerSesion(): Sesion | null {
    const token = this.almacenamiento.getItem('token');
    const sesion = token ? sesionDesdeToken(token) : null;

    if (sesion === null) {
      CLAVES_SESION.forEach((clave) => this.almacenamiento.removeItem(clave));
    } else {
      this.guardarDatosSesion(sesion);
    }

    return sesion;
  }
}
