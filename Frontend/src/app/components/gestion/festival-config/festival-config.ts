import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Observable, forkJoin } from 'rxjs';

import {
  Artista,
  Escenario,
  Festival,
  Presentacion,
  TipoEntrada,
  Zona,
} from '../../../models/modelos';
import { FestivalService } from '../../../services/festival.service';
import { ToastService } from '../../../services/toast.service';
import { Errores, esVacio, hayErrores, mensajeError } from '../../../utils/validaciones';
import { CampoComponent } from '../../campo/campo';
import { ConfirmDialogComponent } from '../../confirm-dialog/confirm-dialog';

interface Eliminacion {
  mensaje: string;
  peticion: () => Observable<unknown>;
}

@Component({
  selector: 'app-festival-config',
  imports: [CurrencyPipe, DatePipe, FormsModule, RouterLink, CampoComponent, ConfirmDialogComponent],
  templateUrl: './festival-config.html',
})
export class FestivalConfigComponent implements OnInit {
  private festivalService = inject(FestivalService);
  private toastService = inject(ToastService);
  private cambios = inject(ChangeDetectorRef);

  private id =Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));

  protected cargando = signal(true);
  protected error = signal('');
  protected errores = signal<Errores>({});

  protected festival = signal<Festival | null>(null);
  protected zonas = signal<Zona[]>([]);
  private todosLosTipos = signal<TipoEntrada[]>([]);
  protected escenarios = signal<Escenario[]>([]);
  protected presentaciones = signal<Presentacion[]>([]);
  protected artistas = signal<Artista[]>([]);
  protected eliminacion = signal<Eliminacion | null>(null);

  protected tipos = computed(() =>
    this.todosLosTipos().filter((tipo) => this.zonas().some((z) => z.idZona === tipo.idZona)),
  );

  protected zonaForm = { idZona: 0, nombre: '', tipoZona: '', aforoMaximo: null as number | null };
  protected tipoForm = {
    idTipoEntrada: 0,
    categoria: '',
    precioBase: null as number | null,
    precioTopeReventa: null as number | null,
    idZona: 0,
  };
  protected escenarioForm = { nombreEscenario: '', capacidad: null as number | null };
  protected presentacionForm = {
    fecha: '',
    inicio: '',
    fin: '',
    nombreEscenario: '',
    idArtista: 0,
  };

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.error.set('');

    forkJoin({
      festival: this.festivalService.buscar(this.id),
      zonas: this.festivalService.zonasDeFestival(this.id),
      tipos: this.festivalService.listarTipos(),
      escenarios: this.festivalService.escenariosDeFestival(this.id),
      presentaciones: this.festivalService.presentacionesDeFestival(this.id),
      artistas: this.festivalService.listarArtistas(),
    }).subscribe({
      next: ({ festival, zonas, tipos, escenarios, presentaciones, artistas }) => {
        this.festival.set(festival);
        this.zonas.set(zonas);
        this.todosLosTipos.set(tipos);
        this.escenarios.set(escenarios);
        this.presentaciones.set(
          [...presentaciones].sort((a, b) => a.horaInicio.localeCompare(b.horaInicio)),
        );
        this.artistas.set(artistas);
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(mensajeError(err, 'No pudimos cargar este festival'));
        this.cargando.set(false);
      },
    });
  }

  protected nombreZona(idZona: number): string {
    return this.zonas().find((zona) => zona.idZona === idZona)?.nombre ?? '';
  }

  protected nombreArtista(idArtista: number): string {
    return (
      this.artistas().find((artista) => artista.idArtista === idArtista)?.nombreArtistico ??
      'Artista eliminado'
    );
  }

  protected guardarZona(): void {
    const f = this.zonaForm;
    const errores: Errores = {};

    if (esVacio(f.nombre)) {
      errores['zonaNombre'] = 'Escribe el nombre';
    }

    if (esVacio(f.tipoZona)) {
      errores['zonaTipo'] = 'Escribe el tipo de zona';
    }

    if (f.aforoMaximo == null || f.aforoMaximo <= 0 || !Number.isInteger(f.aforoMaximo)) {
      errores['zonaAforo'] = 'Escribe el aforo como un número entero';
    }

    if (this.validar(errores)) {
      const zona = { ...f, aforoMaximo: Number(f.aforoMaximo), idFestival: this.id };

      this.ejecutar(
        f.idZona ? this.festivalService.actualizarZona(f.idZona, zona) : this.festivalService.crearZona(zona),
        'Zona guardada',
        () => (this.zonaForm = { idZona: 0, nombre: '', tipoZona: '', aforoMaximo: null }),
      );
    }
  }

  protected editarZona(zona: Zona): void {
    this.zonaForm = { ...zona };
  }

  protected guardarTipo(): void {
    const f = this.tipoForm;
    const errores: Errores = {};

    if (esVacio(f.categoria)) {
      errores['tipoCategoria'] = 'Escribe la categoría';
    }

    if (!f.idZona) {
      errores['tipoZona'] = 'Selecciona la zona';
    }

    if (f.precioBase == null || f.precioBase < 0) {
      errores['tipoPrecio'] = 'Escribe el precio base';
    }

    if (f.precioTopeReventa == null || f.precioBase == null || f.precioTopeReventa < f.precioBase) {
      errores['tipoTope'] = 'Debe ser igual o mayor que el precio base';
    }

    if (this.validar(errores)) {
      const tipo = { ...f, precioBase: Number(f.precioBase), precioTopeReventa: Number(f.precioTopeReventa) };

      this.ejecutar(
        f.idTipoEntrada
          ? this.festivalService.actualizarTipo(f.idTipoEntrada, tipo)
          : this.festivalService.crearTipo(tipo),
        'Entrada guardada',
        () =>
          (this.tipoForm = {
            idTipoEntrada: 0,
            categoria: '',
            precioBase: null,
            precioTopeReventa: null,
            idZona: 0,
          }),
      );
    }
  }

  protected editarTipo(tipo: TipoEntrada): void {
    this.tipoForm = { ...tipo };
  }

  protected guardarEscenario(): void {
    const f = this.escenarioForm;
    const errores: Errores = {};

    if (esVacio(f.nombreEscenario)) {
      errores['escenarioNombre'] = 'Escribe el nombre';
    }

    if (f.capacidad == null || f.capacidad <= 0 || !Number.isInteger(f.capacidad)) {
      errores['escenarioCapacidad'] = 'Escribe la capacidad como un número entero';
    }

    if (this.validar(errores)) {
      this.ejecutar(
        this.festivalService.crearEscenario({
          idFestival: this.id,
          nombreEscenario: f.nombreEscenario.trim(),
          capacidad: Number(f.capacidad),
        }),
        'Escenario guardado',
        () => (this.escenarioForm = { nombreEscenario: '', capacidad: null }),
      );
    }
  }

  protected guardarPresentacion(): void {
    const f = this.presentacionForm;
    const errores: Errores = {};
    const festival = this.festival();

    if (esVacio(f.fecha)) {
      errores['presFecha'] = 'Selecciona la fecha';
    } else if (festival && (f.fecha < festival.fechaInicio || f.fecha > festival.fechaFin)) {
      errores['presFecha'] = 'Debe estar dentro de las fechas del festival';
    }

    if (esVacio(f.inicio)) {
      errores['presInicio'] = 'Selecciona la hora de inicio';
    }

    if (esVacio(f.fin) || f.fin <= f.inicio) {
      errores['presFin'] = 'Debe ser posterior a la hora de inicio';
    }

    if (esVacio(f.nombreEscenario)) {
      errores['presEscenario'] = 'Selecciona el escenario';
    }

    if (!f.idArtista) {
      errores['presArtista'] = 'Selecciona el artista';
    }

    if (this.validar(errores)) {
      this.ejecutar(
        this.festivalService.crearPresentacion({
          fecha: f.fecha,
          horaInicio: `${f.fecha}T${f.inicio}:00`,
          horaFin: `${f.fecha}T${f.fin}:00`,
          idFestival: this.id,
          nombreEscenario: f.nombreEscenario,
          idArtista: f.idArtista,
        }),
        'Presentación agregada',
        () =>
          (this.presentacionForm = { fecha: '', inicio: '', fin: '', nombreEscenario: '', idArtista: 0 }),
      );
    }
  }

  protected eliminarZona(zona: Zona): void {
    this.eliminacion.set({
      mensaje: `Se eliminará la zona ${zona.nombre}.`,
      peticion: () => this.festivalService.eliminarZona(zona.idZona),
    });
  }

  protected eliminarTipo(tipo: TipoEntrada): void {
    this.eliminacion.set({
      mensaje: `Se eliminará la entrada ${tipo.categoria}.`,
      peticion: () => this.festivalService.eliminarTipo(tipo.idTipoEntrada),
    });
  }

  protected eliminarEscenario(escenario: Escenario): void {
    this.eliminacion.set({
      mensaje: `Se eliminará el escenario ${escenario.nombreEscenario}.`,
      peticion: () => this.festivalService.eliminarEscenario(this.id, escenario.nombreEscenario),
    });
  }

  protected eliminarPresentacion(presentacion: Presentacion): void {
    this.eliminacion.set({
      mensaje: `Se eliminará la presentación de ${this.nombreArtista(presentacion.idArtista)}.`,
      peticion: () => this.festivalService.eliminarPresentacion(presentacion.idPresentacion),
    });
  }

  protected confirmarEliminacion(): void {
    const eliminacion = this.eliminacion();

    this.eliminacion.set(null);

    if (eliminacion) {
      this.ejecutar(eliminacion.peticion(), 'Eliminado', () => undefined);
    }
  }

  private validar(errores: Errores): boolean {
    this.errores.set(errores);

    return !hayErrores(errores);
  }

  private ejecutar(peticion: Observable<unknown>, mensaje: string, limpiar: () => void): void {
    peticion.subscribe({
      next: () => {
        this.toastService.success(mensaje);
        limpiar();
        // Los formularios no son signals: sin esto la vista no se entera de que se vaciaron
        this.cambios.markForCheck();
        this.cargar();
      },
      error: (err: HttpErrorResponse) =>
        this.toastService.error(
          'No se pudo completar',
          mensajeError(err, 'Inténtalo de nuevo en unos minutos.'),
        ),
    });
  }
}
