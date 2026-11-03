import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, forkJoin, map } from 'rxjs';

import { environment } from '../../environment/environment';
import { Boleta, BoletaDetalle } from '../models/boleta';
import { FestivalService } from './festival.service';

@Injectable({
  providedIn: 'root',
})
export class BoletaService {
  private http = inject(HttpClient);

  private festivalService = inject(FestivalService);

  listarDetallePorComprador(documento: number): Observable<BoletaDetalle[]> {
    return forkJoin({
      boletas: this.listarPorComprador(documento),
      tipos: this.festivalService.listarTiposEntrada(),
      zonas: this.festivalService.listarZonas(),
      festivales: this.festivalService.listar(),
    }).pipe(
      map(({ boletas, tipos, zonas, festivales }) =>
        boletas.flatMap((boleta) => {
          const tipoEntrada = tipos.find((tipo) => tipo.idTipoEntrada === boleta.idTipoEntrada);
          const zona = zonas.find((item) => item.idZona === tipoEntrada?.idZona);
          const festival = festivales.find((item) => item.idFestival === zona?.idFestival);

          return tipoEntrada && zona && festival
            ? [{ codigoBoleta: boleta.codigoBoleta, fechaEmision: boleta.fechaEmision, tipoEntrada, zona, festival }]
            : [];
        }),
      ),
    );
  }

  // Ruta prevista: aún no hay controlador de boletas en el backend.
  private listarPorComprador(documento: number): Observable<Boleta[]> {
    return this.http.get<Boleta[]>(`${environment.apiUrl}/boletas/comprador/${documento}`);
  }
}
