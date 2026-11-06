// Interfaces con la misma forma que los DTO del backend.

export type TipoUsuario = 'ADMINISTRADOR' | 'ORGANIZADOR' | 'USUARIO';

export const ETIQUETA_ROL: Record<TipoUsuario, string> = {
  ADMINISTRADOR: 'Administrador',
  ORGANIZADOR: 'Organizador',
  USUARIO: 'Usuario',
};

export const INICIO_ROL: Record<TipoUsuario, string> = {
  ADMINISTRADOR: '/app/admin/dashboard',
  ORGANIZADOR: '/app/gestion/festivales',
  USUARIO: '/app/usuario/boletas',
};

export interface LoginDTO {
  nombreUsuario: string;
  contrasenia: string;
}

export interface RegistroDTO {
  documento: number;
  nombreUsuario: string;
  contrasenia: string;
  nombre: string;
  apellido: string;
  correo: string;
  fechaNacimiento: string;
  telefono: string[];
}

export interface RespuestaAutenticacionDTO {
  token: string;
  tipoToken: string;
  documento: number;
  nombreUsuario: string;
  correo: string;
  tipoUsuario: TipoUsuario;
}

export type Sesion = Omit<RespuestaAutenticacionDTO, 'token' | 'tipoToken'>;

export interface Usuario {
  documento: number;
  nombreUsuario: string;
  contrasenia?: string;
  nombre: string;
  apellido: string;
  correo: string;
  fechaNacimiento: string | null;
  fechaRegistro?: string;
  estadoCuenta?: string;
  tipoUsuario?: string;
  telefono: string[];
  autorizadoPorDocumento?: number | null;
  fechaAutorizacion?: string | null;
}

export interface Organizador extends Usuario {
  cargo: string;
  supervisorDocumento?: number | null;
}

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

export interface Escenario {
  idFestival: number;
  nombreEscenario: string;
  capacidad: number;
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
  idGenero: number;
  redesSociales: string[];
  idArtistasInvitados: number[];
}

export interface Genero {
  idGenero: number;
  nombre: string;
  idGeneroPrincipal: number | null;
}

export interface Patrocinador {
  idPatrocinador: number;
  nombre: string;
  tipo: string;
  contacto: string;
}

export interface Patrocinio {
  idPatrocinio: number;
  monto: number;
  tipoAporte: string;
  idFestival: number;
  idPatrocinador: number;
}

export interface Boleta {
  codigoBoleta: number;
  fechaEmision: string;
  idTipoEntrada: number;
  documentoComprador: number;
}

export interface BoletaDetalle extends Boleta {
  tipoEntrada: TipoEntrada;
  zona: Zona;
  festival: Festival;
  transacciones: Transaccion[];
  devuelta: boolean;
}

export interface Dispositivo {
  idDispositivo: number;
  identificadorUnico: string;
  tipo: string;
  activo: boolean;
  documentoUsuario: number;
}

export interface Transaccion {
  idTransaccion: number;
  fechaHora: string;
  precio: number;
  comisionOrganizador: number;
  metodoPago: string;
  codigoBoleta: number;
  idDispositivo: number;
  tipoTransaccion: string;
}

export interface ReventaAutorizada extends Transaccion {
  vendedorDocumento: number;
  compradorDocumento: number;
}

export interface PublicacionReventa {
  /** Identifica la boleta en venta sin revelar su código; cambia cuando la boleta se vende. */
  referencia: number;
  precio: number;
  codigoBoleta: number;
  vendedorDocumento: number;
  idFestival: number;
  nombreFestival: string;
  ciudad: string;
  fechaInicio: string;
  fechaFin: string;
  categoriaEntrada: string;
  nombreZona: string;
}

export interface Devolucion extends Transaccion {
  motivo: string;
  porcentajeReembolso: number;
}

export interface Inscripcion {
  documento: number;
  idLista: number;
  idTipoEntrada: number;
  fechaSolicitud: string;
  estado: string;
}

export interface Notificacion {
  idNotificacion: number;
  fechaHora: string;
  canal: string;
  mensaje: string;
  usuarioDocumento: number;
  idLista: number;
}

export interface ControlAcceso {
  idAcceso: number;
  fechaHoraIngreso: string;
  resultado: string;
  codigoBoleta: number;
  motivo: string;
}

export type EstadoFestival = 'proximo' | 'en-curso' | 'finalizado';

export const ETIQUETA_ESTADO: Record<EstadoFestival, string> = {
  proximo: 'Próximo',
  'en-curso': 'En curso',
  finalizado: 'Finalizado',
};

export const CLASE_ESTADO: Record<EstadoFestival, string> = {
  proximo: 'chip-ok',
  'en-curso': 'chip-alerta',
  finalizado: 'chip-neutro',
};

export const METODOS_PAGO = ['PSE', 'Tarjeta de crédito', 'Tarjeta débito'];

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

// El backend no guarda imágenes de festivales: cada uno usa un cartel genérico según su id.
export function cartelFestival(idFestival: number): string {
  return `img/poster-${(idFestival % 8) + 1}.svg`;
}
