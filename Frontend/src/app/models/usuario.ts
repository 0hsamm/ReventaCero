export type TipoUsuario = 'ADMINISTRADOR' | 'ORGANIZADOR' | 'USUARIO';

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
  tipoUsuario: TipoUsuario | null;
}

export type Sesion = Omit<RespuestaAutenticacionDTO, 'token' | 'tipoToken'>;
