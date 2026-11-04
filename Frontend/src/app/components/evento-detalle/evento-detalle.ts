import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin, map, of, switchMap } from 'rxjs';

import {
  Artista,
  ETIQUETA_ESTADO,
  Festival,
  METODOS_PAGO,
  Presentacion,
  TipoEntrada,
  Zona,
  cartelFestival,
  estadoFestival,
} from '../../models/modelos';
import { AuthService } from '../../services/auth.service';
import { BoletaService } from '../../services/boleta.service';
import { FestivalService } from '../../services/festival.service';
import { ListaEsperaService } from '../../services/lista-espera.service';
import { ToastService } from '../../services/toast.service';
import { mensajeError } from '../../utils/validaciones';
import { CampoComponent } from '../campo/campo';

const MAXIMO_POR_COMPRA = 6;

interface Opcion {
  tipo: TipoEntrada;
  zona: Zona;
  disponibles: number;
}

@Component({
  selector: 'app-evento-detalle',
  imports: [CurrencyPipe, DatePipe, DecimalPipe, FormsModule, RouterLink, CampoComponent],
  templateUrl: './evento-detalle.html',
  styleUrl: './evento-detalle.css',
})
export class EventoDetalleComponent implements OnInit {
  private festivalService = inject(FestivalService);
  private boletaService = inject(BoletaService);
  private listaService = inject(ListaEsperaService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private toastService = inject(ToastService);
  private ruta = inject(ActivatedRoute).snapshot;

  private id = Number(this.ruta.paramMap.get('id'));

  protected readonly metodos = METODOS_PAGO;
  protected readonly etiquetaEstado = ETIQUETA_ESTADO;
  protected readonly estadoFestival = estadoFestival;
  protected readonly cartelFestival = cartelFestival;

  protected sesion = this.authService.sesion;
  protected esUsuario = computed(() => this.sesion()?.tipoUsuario === 'USUARIO');

  protected cargando = signal(true);
  protected noExiste = signal(false);
  protected errorCarga = signal('');
  protected errorDetalle = signal('');

  protected festival = signal<Festival | null>(null);
  protected opciones = signal<Opcion[]>([]);
  private presentaciones = signal<Presentacion[]>([]);
  private artistas = signal<Artista[]>([]);

  protected elegida = signal<Opcion | null>(null);
  protected cantidad = signal(1);
  protected metodo = '';

  protected comprando = signal(false);
  protected error = signal('');
  protected errorOpcion = signal('');
  protected errorMetodo = signal('');

  protected finalizado = computed(() => {
    const festival = this.festival();

    return festival != null && estadoFestival(festival) === 'finalizado';
  });

  protected maximo = computed(() =>
    Math.min(MAXIMO_POR_COMPRA, this.elegida()?.disponibles ?? MAXIMO_POR_COMPRA),
  );

  protected total = computed(() => (this.elegida()?.tipo.precioBase ?? 0) * this.cantidad());

  protected cartel = computed(() =>
    [...this.presentaciones()]
      .sort((a, b) => a.horaInicio.localeCompare(b.horaInicio))
      .map((presentacion) => ({
        ...presentacion,
        artista:
          this.artistas().find((artista) => artista.idArtista === presentacion.idArtista)
            ?.nombreArtistico ?? 'Por confirmar',
      })),
  );

  ngOnInit(): void {
    this.festivalService.buscar(this.id).subscribe({
      next: (festival) => {
        this.festival.set(festival);
        this.cargando.set(false);

        // El backend solo muestra zonas, precios y cartel a quien inició sesión
        if (this.sesion()) {
          this.cargarDetalle();
        }
      },
      error: (err: HttpErrorResponse) => {
        this.noExiste.set(err.status === 404);
        this.errorCarga.set(mensajeError(err, 'No pudimos cargar este festival'));
        this.cargando.set(false);
      },
    });
  }

  private cargarDetalle(): void {
    forkJoin({
      zonas: this.festivalService.zonasDeFestival(this.id),
      tipos: this.festivalService.listarTipos(),
      presentaciones: this.festivalService.presentacionesDeFestival(this.id),
      artistas: this.festivalService.listarArtistas(),
    })
      .pipe(
        switchMap(({ zonas, tipos, presentaciones, artistas }) => {
          this.presentaciones.set(presentaciones);
          this.artistas.set(artistas);

          const aforos = zonas.map((zona) =>
            this.festivalService
              .aforoDisponible(zona.idZona)
              .pipe(map((disponibles) => ({ zona, disponibles }))),
          );

          return (aforos.length ? forkJoin(aforos) : of([])).pipe(
            map((conAforo) =>
              conAforo.flatMap(({ zona, disponibles }) =>
                tipos
                  .filter((tipo) => tipo.idZona === zona.idZona)
                  .map((tipo) => ({ tipo, zona, disponibles })),
              ),
            ),
          );
        }),
      )
      .subscribe({
        next: (opciones) => this.opciones.set(opciones),
        error: (err: HttpErrorResponse) =>
          this.errorDetalle.set(mensajeError(err, 'No pudimos cargar las boletas')),
      });
  }

  protected irAIngresar(): void {
    this.router.navigate(['/acceso'], { queryParams: { volver: `/eventos/${this.id}` } });
  }

  protected elegir(opcion: Opcion): void {
    this.elegida.set(opcion);
    this.errorOpcion.set('');
    this.cantidad.update((valor) => Math.max(1, Math.min(valor, this.maximo())));
  }

  protected cambiarCantidad(cambio: number): void {
    this.cantidad.update((valor) => Math.min(this.maximo(), Math.max(1, valor + cambio)));
  }

  protected comprar(): void {
    const opcion = this.elegida();

    this.error.set('');
    this.errorOpcion.set(opcion ? '' : 'Selecciona una boleta');
    this.errorMetodo.set(this.metodo ? '' : 'Selecciona un método de pago');

    if (opcion == null || !this.metodo || this.comprando()) {
      return;
    }

    this.comprando.set(true);

    this.boletaService.comprar(opcion.tipo.idTipoEntrada, this.metodo, this.cantidad()).subscribe({
      next: () => {
        this.comprando.set(false);
        this.toastService.success('Compra registrada');
        this.router.navigate(['/app/usuario/boletas']);
      },
      error: (err: HttpErrorResponse) => {
        this.comprando.set(false);
        this.error.set(
          `${mensajeError(err, 'No se pudo completar la compra')} Revisa Mis boletas: si comprabas varias, algunas pudieron registrarse.`,
        );
      },
    });
  }

  protected unirseALista(opcion: Opcion): void {
    const sesion = this.sesion();

    if (sesion == null) {
      return;
    }

    this.listaService.inscribir(sesion.documento, opcion.tipo.idTipoEntrada).subscribe({
      next: () => this.toastService.success('Te uniste a la lista de espera'),
      error: (err: HttpErrorResponse) =>
        this.toastService.error(
          'No pudimos inscribirte',
          mensajeError(err, 'Inténtalo de nuevo en unos minutos.'),
        ),
    });
  }
}
