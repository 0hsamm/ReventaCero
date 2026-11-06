import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, concatMap, forkJoin, from, map, of, switchMap, toArray } from 'rxjs';

import { environment } from '../../environment/environment';
import {
  Boleta,
  BoletaDetalle,
  Devolucion,
  Dispositivo,
  ReventaAutorizada,
  Transaccion,
} from '../models/modelos';
import { FestivalService } from './festival.service';

@Injectable({
  providedIn: 'root',
})
export class BoletaService {
  private http = inject(HttpClient);
  private festivalService = inject(FestivalService);

  private api = environment.apiUrl;

  boletasDetalle(documento: number): Observable<BoletaDetalle[]> {
    return this.http.get<Boleta[]>(`${this.api}/boletas/comprador/${documento}`).pipe(
      switchMap((boletas) =>
        forkJoin({
          tipos: this.festivalService.listarTipos(),
          zonas: this.festivalService.listarZonas(),
          festivales: this.festivalService.listar(),
          // Una boleta devuelta sigue en la cuenta: solo su historial dice que ya no es válida
          historiales: boletas.length
            ? forkJoin(boletas.map((b) => this.transaccionesDeBoleta(b.codigoBoleta)))
            : of<Transaccion[][]>([]),
        }).pipe(
          map(({ tipos, zonas, festivales, historiales }) =>
            boletas.flatMap((boleta, i) => {
              const tipoEntrada = tipos.find((t) => t.idTipoEntrada === boleta.idTipoEntrada);
              const zona = zonas.find((z) => z.idZona === tipoEntrada?.idZona);
              const festival = festivales.find((f) => f.idFestival === zona?.idFestival);

              return tipoEntrada && zona && festival
                ? [
                    {
                      ...boleta,
                      tipoEntrada,
                      zona,
                      festival,
                      transacciones: historiales[i],
                      devuelta: historiales[i].some((t) => t.tipoTransaccion === 'DEVOLUCION'),
                    },
                  ]
                : [];
            }),
          ),
        ),
      ),
    );
  }

  /** Compra una boleta por cada unidad: el backend registra una por venta. */
  comprar(idTipoEntrada: number, metodoPago: string, cantidad: number): Observable<unknown[]> {
    return this.dispositivoActivo().pipe(
      switchMap((dispositivo) =>
        from(Array.from({ length: cantidad })).pipe(
          concatMap(() =>
            this.http.post(`${this.api}/ventas-primarias`, {
              idTipoEntrada,
              idDispositivo: dispositivo.idDispositivo,
              metodoPago,
              canalVenta: 'WEB',
            }),
          ),
          toArray(),
        ),
      ),
    );
  }

  devolver(codigoBoleta: number, motivo: string, metodoPago: string): Observable<Devolucion> {
    return this.dispositivoActivo().pipe(
      switchMap((dispositivo) =>
        this.http.post<Devolucion>(`${this.api}/devoluciones`, {
          codigoBoleta,
          motivo,
          metodoPago,
          porcentajeReembolso: 100,
          idDispositivo: dispositivo.idDispositivo,
        }),
      ),
    );
  }

  qr(codigoBoleta: number): Observable<Blob> {
    return this.http.get(`${this.api}/boletas/${codigoBoleta}/qr`, { responseType: 'blob' });
  }

  qrTexto(codigoBoleta: number): Observable<string> {
    return this.http.get(`${this.api}/boletas/${codigoBoleta}/qr/texto`, { responseType: 'text' });
  }

  transaccionesDeBoleta(codigoBoleta: number): Observable<Transaccion[]> {
    return this.http.get<Transaccion[]>(`${this.api}/transacciones/boleta/${codigoBoleta}`);
  }

  reventasComoVendedor(documento: number): Observable<ReventaAutorizada[]> {
    return this.http.get<ReventaAutorizada[]>(
      `${this.api}/reventas-autorizadas/vendedor/${documento}`,
    );
  }

  reventasComoComprador(documento: number): Observable<ReventaAutorizada[]> {
    return this.http.get<ReventaAutorizada[]>(
      `${this.api}/reventas-autorizadas/comprador/${documento}`,
    );
  }

  dispositivos(): Observable<Dispositivo[]> {
    return this.http.get<Dispositivo[]>(
      `${this.api}/dispositivos/usuario/${BoletaService.documento()}`,
    );
  }

  cambiarEstadoDispositivo(id: number, activo: boolean): Observable<Dispositivo> {
    return this.http.put<Dispositivo>(`${this.api}/dispositivos/${id}/estado`, { activo });
  }

  // El backend exige un dispositivo activo para toda transacción: este navegador se registra solo.
  dispositivoActivo(): Observable<Dispositivo> {
    return this.dispositivos().pipe(
      switchMap((lista) => {
        const activo = lista.find((dispositivo) => dispositivo.activo);

        return activo
          ? of(activo)
          : this.http.post<Dispositivo>(`${this.api}/dispositivos`, {
              identificadorUnico: `web-${crypto.randomUUID()}`,
              tipo: 'WEB',
              documentoUsuario: BoletaService.documento(),
            });
      }),
    );
  }

  private static documento(): number {
    return Number(localStorage.getItem('documento'));
  }
}
