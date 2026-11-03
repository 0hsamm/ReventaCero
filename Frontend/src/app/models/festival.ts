export interface Festival {
  idFestival: number;
  nombre: string;
  edicion: number;
  fechaInicio: string;
  fechaFin: string;
  ubicacionPais: string;
  ubicacionCiudad: string;
  ubicacionDireccion: string;
  documentoOrganizador: number;
}

export interface Zona {
  idZona: number;
  nombre: string;
  tipoZona: string;
  aforoMaximo: number;
  idFestival: number;
}

export interface TipoEntrada {
  idTipoEntrada: number;
  categoria: string;
  precioBase: number;
  precioTopeReventa: number;
  idZona: number;
}

export interface Presentacion {
  idPresentacion: number;
  fecha: string;
  horaInicio: string;
  horaFin: string;
  idFestival: number;
  nombreEscenario: string;
  idArtista: number;
}

export interface Artista {
  idArtista: number;
  nombreArtistico: string;
  pais: string;
}

export interface DetalleFestival {
  zonas: Zona[];
  tiposEntrada: TipoEntrada[];
  presentaciones: Presentacion[];
  artistas: Artista[];
}

export type EstadoFestival = 'proximo' | 'en-curso' | 'finalizado';

export const ETIQUETA_ESTADO: Record<EstadoFestival, string> = {
  proximo: 'Próximo',
  'en-curso': 'En curso',
  finalizado: 'Finalizado',
};

export function hoyIso(): string {
  const hoy = new Date();
  const mes = String(hoy.getMonth() + 1).padStart(2, '0');
  const dia = String(hoy.getDate()).padStart(2, '0');

  return `${hoy.getFullYear()}-${mes}-${dia}`;
}

export function estadoFestival(festival: Festival): EstadoFestival {
  const hoy = hoyIso();

  if (festival.fechaFin < hoy) {
    return 'finalizado';
  }

  return festival.fechaInicio > hoy ? 'proximo' : 'en-curso';
}

export function diasHasta(fechaIso: string): number {
  const [anio, mes, dia] = fechaIso.split('-').map(Number);
  const [anioHoy, mesHoy, diaHoy] = hoyIso().split('-').map(Number);

  return Math.round(
    (Date.UTC(anio, mes - 1, dia) - Date.UTC(anioHoy, mesHoy - 1, diaHoy)) / 86400000,
  );
}
