import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, map, of, switchMap } from 'rxjs';

import { Inscripcion } from '../../../models/modelos';
import { AuthService } from '../../../services/auth.service';
import { FestivalService } from '../../../services/festival.service';
import { ListaEsperaService } from '../../../services/lista-espera.service';
import { ToastService } from '../../../services/toast.service';
import { mensajeError } from '../../../utils/validaciones';
import { ConfirmDialogComponent } from '../../confirm-dialog/confirm-dialog';

interface Fila extends Inscripcion {
  descripcion: string;
  posicion: number | null;
}

@Component({
  selector: 'app-listas-espera',
  imports: [DatePipe, RouterLink, ConfirmDialogComponent],
  templateUrl: './listas-espera.html',
})
export class ListasEsperaComponent implements OnInit {
  private listaService = inject(ListaEsperaService);
  private festivalService = inject(FestivalService);
  private authService = inject(AuthService);
  private toastService = inject(ToastService);

  private documento = this.authService.sesion()?.documento ?? 0;

  protected cargando = signal(true);
  protected error = signal('');
  protected filas = signal<Fila[]>([]);
  protected cancelando = signal<Fila | null>(null);

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    forkJoin({
      inscripciones: this.listaService.inscripciones(this.documento),
      tipos: this.festivalService.listarTipos(),
      zonas: this.festivalService.listarZonas(),
      festivales: this.festivalService.listar(),
    })
      .pipe(
        map(({ inscripciones, tipos, zonas, festivales }) =>
          inscripciones.map((inscripcion) => {
            const tipo = tipos.find((t) => t.idTipoEntrada === inscripcion.idTipoEntrada);
            const zona = zonas.find((z) => z.idZona === tipo?.idZona);
            const festival = festivales.find((f) => f.idFestival === zona?.idFestival);

            return {
              ...inscripcion,
              posicion: null,
              descripcion: festival
                ? `${festival.nombre} · ${zona?.nombre} · ${tipo?.categoria}`
                : `Entrada ${inscripcion.idTipoEntrada}`,
            } as Fila;
          }),
        ),
        switchMap((filas) => (filas.length ? forkJoin(filas.map((f) => this.conPosicion(f))) : of([]))),
      )
      .subscribe({
        next: (filas) => {
          this.filas.set(filas);
          this.cargando.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.error.set(mensajeError(err, 'No pudimos cargar tus listas de espera'));
          this.cargando.set(false);
        },
      });
  }

  protected confirmarCancelacion(): void {
    const fila = this.cancelando();

    this.cancelando.set(null);

    if (fila == null) {
      return;
    }

    this.listaService.cancelar(this.documento, fila.idLista).subscribe({
      next: () => {
        this.toastService.success('Inscripción cancelada');
        this.cargar();
      },
      error: (err: HttpErrorResponse) =>
        this.toastService.error('No se pudo cancelar', mensajeError(err, 'Inténtalo de nuevo.')),
    });
  }

  protected claseEstado(estado: string): string {
    if (estado === 'NOTIFICADA') {
      return 'chip-ok';
    }

    return estado === 'CANCELADA' ? 'chip-neutro' : 'chip-alerta';
  }

  // Solo las inscripciones pendientes tienen posición en la cola
  private conPosicion(fila: Fila) {
    if (fila.estado !== 'PENDIENTE') {
      return of(fila);
    }

    return this.listaService.posicion(this.documento, fila.idLista).pipe(
      map((posicion) => ({ ...fila, posicion })),
      catchError(() => of(fila)),
    );
  }
}
