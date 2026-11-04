import { HttpErrorResponse } from '@angular/common/http';

export type Errores = Record<string, string>;

export const LONGITUD_MINIMA_CONTRASENIA = 8;

export function esCorreo(valor: string): boolean {
  return /^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(valor.trim());
}

export function esVacio(valor: unknown): boolean {
  return valor == null || String(valor).trim() === '';
}

export function hayErrores(errores: Errores): boolean {
  return Object.keys(errores).length > 0;
}

export function limpiarTelefono(valor: string): string {
  return valor.replace(/[\s()-]/g, '');
}

export function esTelefono(valor: string): boolean {
  return /^\+?\d{7,15}$/.test(limpiarTelefono(valor));
}

export function tieneLetras(valor: string): boolean {
  return /\p{L}{2,}/u.test(valor);
}

export function esDocumento(valor: unknown): boolean {
  return /^[1-9]\d{4,14}$/.test(String(valor ?? '').trim());
}

// El backend responde sus reglas de negocio como texto: se muestran tal cual. Un 500 trae un
// objeto con detalles internos, y ese nunca se muestra.
export function mensajeError(err: HttpErrorResponse, porDefecto: string): string {
  if (err.status === 0) {
    return 'No hay conexión con el servidor. Inténtalo de nuevo en unos minutos.';
  }

  if (err.status >= 500) {
    return 'El servidor tuvo un problema. Inténtalo de nuevo en unos minutos.';
  }

  if (err.status === 403 && typeof err.error !== 'string') {
    return 'No tienes permiso para hacer esto.';
  }

  return typeof err.error === 'string' && err.error ? err.error : porDefecto;
}
