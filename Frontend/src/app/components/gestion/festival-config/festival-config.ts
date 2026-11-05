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
import { PatrociniosFestivalComponent } from '../patrocinios-festival/patrocinios-festival';

interface Eliminacion {
  mensaje: string;
  peticion: () => Observable<unknown>;
}

@Component({
  selector: 'app-festival-config',
  imports: [
    CurrencyPipe,
    DatePipe,
    FormsModule,
    RouterLink,
    CampoComponent,
    ConfirmDialogComponent,
    PatrociniosFestivalComponent,
  ],
  templateUrl: './festival-config.html',
})
export class FestivalConfigComponent implements OnInit {
  private festivalService = inject(FestivalService);
  private toastService = inject(ToastService);
  private cambios = inject(ChangeDetectorRef);

  private id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));

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
    const formularioZona = this.zonaForm;
    const errores: Errores = {};

    if (esVacio(formularioZona.nombre)) {
      errores['zonaNombre'] = 'Escribe el nombre';
    }

    if (esVacio(formularioZona.tipoZona)) {
      errores['zonaTipo'] = 'Escribe el tipo de zona';
    }

    if (
      formularioZona.aforoMaximo == null ||
      formularioZona.aforoMaximo <= 0 ||
      !Number.isInteger(formularioZona.aforoMaximo)
    ) {
      errores['zonaAforo'] = 'Escribe el aforo como un número entero';
    }

    if (this.validar(errores)) {
      const zona = {
        ...formularioZona,
        aforoMaximo: Number(formularioZona.aforoMaximo),
        idFestival: this.id,
      };

      this.ejecutar(
        formularioZona.idZona
          ? this.festivalService.actualizarZona(formularioZona.idZona, zona)
          : this.festivalService.crearZona(zona),
        'Zona guardada',
        () => (this.zonaForm = { idZona: 0, nombre: '', tipoZona: '', aforoMaximo: null }),
      );
    }
  }

  protected editarZona(zona: Zona): void {
    this.zonaForm = { ...zona };
  }

  protected guardarTipo(): void {
    const formularioTipo = this.tipoForm;
    const errores: Errores = {};

    if (esVacio(formularioTipo.categoria)) {
      errores['tipoCategoria'] = 'Escribe la categoría';
    }

    if (!formularioTipo.idZona) {
      errores['tipoZona'] = 'Selecciona la zona';
    }

    if (formularioTipo.precioBase == null || formularioTipo.precioBase < 0) {
      errores['tipoPrecio'] = 'Escribe el precio base';
    }

    if (
      formularioTipo.precioTopeReventa == null ||
      formularioTipo.precioBase == null ||
      formularioTipo.precioTopeReventa < formularioTipo.precioBase
    ) {
      errores['tipoTope'] = 'Debe ser igual o mayor que el precio base';
    }

    if (this.validar(errores)) {
      const tipo = {
        ...formularioTipo,
        precioBase: Number(formularioTipo.precioBase),
        precioTopeReventa: Number(formularioTipo.precioTopeReventa),
      };

      this.ejecutar(
        formularioTipo.idTipoEntrada
          ? this.festivalService.actualizarTipo(formularioTipo.idTipoEntrada, tipo)
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
    const formularioEscenario = this.escenarioForm;
    const errores: Errores = {};

    if (esVacio(formularioEscenario.nombreEscenario)) {
      errores['escenarioNombre'] = 'Escribe el nombre';
    }

    if (
      formularioEscenario.capacidad == null ||
      formularioEscenario.capacidad <= 0 ||
      !Number.isInteger(formularioEscenario.capacidad)
    ) {
      errores['escenarioCapacidad'] = 'Escribe la capacidad como un número entero';
    }

    if (this.validar(errores)) {
      this.ejecutar(
        this.festivalService.crearEscenario({
          idFestival: this.id,
          nombreEscenario: formularioEscenario.nombreEscenario.trim(),
          capacidad: Number(formularioEscenario.capacidad),
        }),
        'Escenario guardado',
        () => (this.escenarioForm = { nombreEscenario: '', capacidad: null }),
      );
    }
  }

  protected guardarPresentacion(): void {
    const formularioPresentacion = this.presentacionForm;
    const errores: Errores = {};
    const festival = this.festival();

    if (esVacio(formularioPresentacion.fecha)) {
      errores['presFecha'] = 'Selecciona la fecha';
    } else if (
      festival &&
      (formularioPresentacion.fecha < festival.fechaInicio ||
        formularioPresentacion.fecha > festival.fechaFin)
    ) {
      errores['presFecha'] = 'Debe estar dentro de las fechas del festival';
    }

    if (esVacio(formularioPresentacion.inicio)) {
      errores['presInicio'] = 'Selecciona la hora de inicio';
    }

    if (
      esVacio(formularioPresentacion.fin) ||
      formularioPresentacion.fin <= formularioPresentacion.inicio
    ) {
      errores['presFin'] = 'Debe ser posterior a la hora de inicio';
    }

    if (esVacio(formularioPresentacion.nombreEscenario)) {
      errores['presEscenario'] = 'Selecciona el escenario';
    }

    if (!formularioPresentacion.idArtista) {
      errores['presArtista'] = 'Selecciona el artista';
    }

    if (this.validar(errores)) {
      this.ejecutar(
        this.festivalService.crearPresentacion({
          fecha: formularioPresentacion.fecha,
          horaInicio: `${formularioPresentacion.fecha}T${formularioPresentacion.inicio}:00`,
          horaFin: `${formularioPresentacion.fecha}T${formularioPresentacion.fin}:00`,
          idFestival: this.id,
          nombreEscenario: formularioPresentacion.nombreEscenario,
          idArtista: formularioPresentacion.idArtista,
        }),
        'Presentación agregada',
        () =>
          (this.presentacionForm = {
            fecha: '',
            inicio: '',
            fin: '',
            nombreEscenario: '',
            idArtista: 0,
          }),
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
