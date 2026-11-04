import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';

import { Artista, Genero } from '../../../models/modelos';
import { FestivalService } from '../../../services/festival.service';
import { ToastService } from '../../../services/toast.service';
import { subirPanel } from '../../../utils/panel';
import { Errores, esVacio, hayErrores, mensajeError } from '../../../utils/validaciones';
import { CampoComponent } from '../../campo/campo';
import { ConfirmDialogComponent } from '../../confirm-dialog/confirm-dialog';
import { PaginacionComponent, TAMANO_PAGINA } from '../../paginacion/paginacion';

@Component({
  selector: 'app-gestion-artistas',
  imports: [FormsModule, CampoComponent, ConfirmDialogComponent, PaginacionComponent],
  templateUrl: './artistas.html',
})
export class GestionArtistasComponent implements OnInit {
  private festivalService = inject(FestivalService);
  private toastService = inject(ToastService);

  protected cargando = signal(true);
  protected error = signal('');
  protected artistas = signal<Artista[]>([]);
  protected generos = signal<Genero[]>([]);
  protected busqueda = signal('');
  protected pagina = signal(1);

  protected editando = signal<Artista | null>(null);
  protected eliminando = signal<Artista | null>(null);
  protected errorForm = signal('');
  protected errores = signal<Errores>({});

  protected nombreArtistico = '';
  protected pais = '';
  protected idGenero = 0;
  protected redes = '';
  protected nuevoGenero = '';

  private filtrados = computed(() => {
    const busqueda = this.busqueda().trim().toLowerCase();

    return this.artistas().filter((artista) =>
      artista.nombreArtistico.toLowerCase().includes(busqueda),
    );
  });

  protected total = computed(() => this.filtrados().length);
  protected filas = computed(() =>
    this.filtrados().slice((this.pagina() - 1) * TAMANO_PAGINA, this.pagina() * TAMANO_PAGINA),
  );

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.error.set('');

    forkJoin({
      artistas: this.festivalService.listarArtistas(),
      generos: this.festivalService.listarGeneros(),
    }).subscribe({
      next: ({ artistas, generos }) => {
        this.artistas.set(artistas);
        this.generos.set(generos);
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(mensajeError(err, 'No pudimos cargar los artistas'));
        this.cargando.set(false);
      },
    });
  }

  protected buscar(valor: string): void {
    this.busqueda.set(valor);
    this.pagina.set(1);
  }

  protected nombreGenero(idGenero: number): string {
    return this.generos().find((genero) => genero.idGenero === idGenero)?.nombre ?? '—';
  }

  protected editar(artista: Artista): void {
    this.editando.set(artista);
    this.nombreArtistico = artista.nombreArtistico;
    this.pais = artista.pais;
    this.idGenero = artista.idGenero;
    this.redes = artista.redesSociales.join(', ');
    this.errores.set({});
    subirPanel();
  }

  protected limpiar(): void {
    this.editando.set(null);
    this.nombreArtistico = '';
    this.pais = '';
    this.idGenero = 0;
    this.redes = '';
    this.errores.set({});
    this.errorForm.set('');
  }

  protected guardar(): void {
    const errores: Errores = {};

    if (esVacio(this.nombreArtistico)) {
      errores['nombreArtistico'] = 'Escribe el nombre artístico';
    }

    if (!this.idGenero) {
      errores['idGenero'] = 'Selecciona el género';
    }

    this.errores.set(errores);
    this.errorForm.set('');

    if (hayErrores(errores)) {
      return;
    }

    const original = this.editando();
    const artista: Partial<Artista> = {
      nombreArtistico: this.nombreArtistico.trim(),
      pais: this.pais.trim(),
      idGenero: this.idGenero,
      redesSociales: this.redes
        .split(',')
        .map((red) => red.trim())
        .filter(Boolean),
      idArtistasInvitados: original?.idArtistasInvitados ?? [],
    };
    const peticion = original
      ? this.festivalService.actualizarArtista(original.idArtista, artista)
      : this.festivalService.crearArtista(artista);

    peticion.subscribe({
      next: () => {
        this.toastService.success(original ? 'Artista actualizado' : 'Artista creado');
        this.limpiar();
        this.cargar();
      },
      error: (err: HttpErrorResponse) =>
        this.errorForm.set(mensajeError(err, 'No se pudo guardar el artista')),
    });
  }

  protected agregarGenero(): void {
    if (esVacio(this.nuevoGenero)) {
      this.errores.set({ nuevoGenero: 'Escribe el nombre del género' });
      return;
    }

    this.festivalService.crearGenero(this.nuevoGenero.trim()).subscribe({
      next: (genero) => {
        this.generos.update((lista) => [...lista, genero]);
        this.idGenero = genero.idGenero;
        this.nuevoGenero = '';
        this.errores.set({});
      },
      error: (err: HttpErrorResponse) =>
        this.errores.set({ nuevoGenero: mensajeError(err, 'No se pudo crear el género') }),
    });
  }

  protected confirmarEliminacion(): void {
    const artista = this.eliminando();

    this.eliminando.set(null);

    if (artista == null) {
      return;
    }

    this.festivalService.eliminarArtista(artista.idArtista).subscribe({
      next: () => {
        this.toastService.success('Artista eliminado');
        this.cargar();
      },
      error: (err: HttpErrorResponse) =>
        this.toastService.error('No se pudo eliminar', mensajeError(err, 'Inténtalo de nuevo.')),
    });
  }
}
