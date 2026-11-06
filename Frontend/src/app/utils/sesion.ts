import type { Sesion, TipoUsuario } from '../models/modelos';

function esTipoUsuario(valor: unknown): valor is TipoUsuario {
  return valor === 'ADMINISTRADOR' || valor === 'ORGANIZADOR' || valor === 'USUARIO';
}

// RF-10: el JWT orienta el panel; el backend valida su firma y autoriza cada operación.
export function sesionDesdeToken(token: string): Sesion | null {
  try {
    const partes = token.split('.');

    if (partes.length !== 3 || partes.some((parte) => !parte)) {
      return null;
    }

    const contenido = atob(partes[1].replace(/-/g, '+').replace(/_/g, '/'));
    const texto = new TextDecoder().decode(
      Uint8Array.from(contenido, (caracter) => caracter.charCodeAt(0)),
    );
    const datos: unknown = JSON.parse(texto);

    if (typeof datos !== 'object' || datos === null) {
      return null;
    }

    const { exp, documento, nombreUsuario, correo, tipoUsuario } = datos as Record<string, unknown>;

    if (
      typeof exp !== 'number' ||
      !Number.isFinite(exp) ||
      exp * 1000 <= Date.now() ||
      typeof documento !== 'number' ||
      !Number.isSafeInteger(documento) ||
      documento <= 0 ||
      typeof nombreUsuario !== 'string' ||
      typeof correo !== 'string' ||
      !esTipoUsuario(tipoUsuario)
    ) {
      return null;
    }

    return { documento, nombreUsuario, correo, tipoUsuario };
  } catch {
    return null;
  }
}
