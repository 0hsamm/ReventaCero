// Contenido provisional de la landing: fechas, artistas, precios e imágenes son ficticios.
// Reemplazar por los datos reales del festival antes de publicar.

export interface ArtistaLineup {
  nombre: string;
  dia: string;
  imagen: string;
  tamano: 'principal' | 'ancho' | 'normal';
}

export type EstadoFase = 'agotada' | 'activa' | 'proxima';

export interface FaseBoleta {
  nombre: string;
  descripcion: string;
  precio: number;
  topeReventa: number;
  estado: EstadoFase;
  codigo: string;
}

export const FESTIVAL = {
  edicion: 'Edición 01',
  fechas: '15—17 ENE 2027',
  ciudad: 'Bogotá, Colombia',
};

export const ANUNCIOS = 'Lineup confirmado • Tickets fase 2 • Reventa con precio tope • ';

export const ARTISTAS: ArtistaLineup[] = [
  { nombre: 'Los Páramos', dia: 'Sáb 16', imagen: 'img/los-paramos.svg', tamano: 'principal' },
  { nombre: 'Marea Baja', dia: 'Vie 15', imagen: 'img/marea-baja.svg', tamano: 'normal' },
  { nombre: 'Ana Quintero', dia: 'Vie 15', imagen: 'img/ana-quintero.svg', tamano: 'normal' },
  { nombre: 'Vela Norte', dia: 'Sáb 16', imagen: 'img/vela-norte.svg', tamano: 'normal' },
  { nombre: 'Voltio Sur', dia: 'Dom 17', imagen: 'img/voltio-sur.svg', tamano: 'normal' },
  { nombre: 'La Sucursal', dia: 'Dom 17', imagen: 'img/la-sucursal.svg', tamano: 'ancho' },
  {
    nombre: 'Cuarteto Andino',
    dia: 'Sáb 16',
    imagen: 'img/cuarteto-andino.svg',
    tamano: 'normal',
  },
  {
    nombre: 'Tomás Rivera Trío',
    dia: 'Dom 17',
    imagen: 'img/tomas-rivera-trio.svg',
    tamano: 'normal',
  },
];

export const FASES: FaseBoleta[] = [
  {
    nombre: 'Fase 1',
    descripcion: 'Abono de tres días. Acceso general.',
    precio: 180000,
    topeReventa: 198000,
    estado: 'agotada',
    codigo: 'F1',
  },
  {
    nombre: 'Fase 2',
    descripcion: 'Abono de tres días. Acceso general.',
    precio: 240000,
    topeReventa: 264000,
    estado: 'activa',
    codigo: 'F2',
  },
  {
    nombre: 'VIP',
    descripcion: 'Abono de tres días. Zona VIP frente al escenario principal.',
    precio: 620000,
    topeReventa: 682000,
    estado: 'proxima',
    codigo: 'V1',
  },
];
