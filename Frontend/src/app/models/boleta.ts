import { Festival, TipoEntrada, Zona } from './festival';

export interface Boleta {
  codigoBoleta: number;
  fechaEmision: string;
  idTipoEntrada: number;
  documentoComprador: number;
}

export interface BoletaDetalle {
  codigoBoleta: number;
  fechaEmision: string;
  tipoEntrada: TipoEntrada;
  zona: Zona;
  festival: Festival;
}
