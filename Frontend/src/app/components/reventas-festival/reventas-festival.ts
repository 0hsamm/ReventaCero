import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  ElementRef,
  OnInit,
  computed,
  inject,
  input,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { METODOS_PAGO, PublicacionReventa } from '../../models/modelos';
import { AuthService } from '../../services/auth.service';
import { PublicacionReventaService } from '../../services/publicacion-reventa.service';
import { ToastService } from '../../services/toast.service';
import { mensajeError } from '../../utils/validaciones';
import { BoletaResumen, DialogoSesionComponent } from '../dialogo-sesion/dialogo-sesion';

@Component({
  selector: 'app-reventas-festival',
  imports: [CurrencyPipe, FormsModule, DialogoSesionComponent],
  templateUrl: './reventas-festival.html',
  styleUrl: './reventas-festival.css',
})
export class ReventasFestivalComponent implements OnInit {
  private publicacionService = inject(PublicacionReventaService);
  private authService = inject(AuthService);
  private toastService = inject(ToastService);
  private router = inject(Router);

  readonly idFestival = input.required<number>();
  readonly finalizado = input(false);
  readonly cantidad = output<number>();

  protected readonly metodos = METODOS_PAGO;

  private dialogoSesion = viewChild<DialogoSesionComponent>('dialogoSesion');
  private panelCompra = viewChild<ElementRef<HTMLElement>>('panelCompra');

  protected sesion = this.authService.sesion;
  protected esUsuario = computed(() => this.sesion()?.tipoUsuario === 'USUARIO');

  protected cargando = signal(true);
  protected error = signal('');
  protected publicaciones = signal<PublicacionReventa[]>([]);
  protected seleccionada = signal<PublicacionReventa | null>(null);
  protected pendiente = signal<BoletaResumen | null>(null);

  protected metodo = '';
  protected errorMetodo = signal('');
  protected errorCompra = signal('');
  protected enviando = signal(false);

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set('');

    this.publicacionService.disponibles().subscribe({
      next: (lista) => {
        const delFestival = lista
          .filter((publicacion) => publicacion.idFestival === this.idFestival())
          .sort((a, b) => a.precio - b.precio);

        this.publicaciones.set(delFestival);
        this.cantidad.emit(delFestival.length);
        this.cargando.set(false);

        const elegida = this.seleccionada();

        if (elegida && !delFestival.some((p) => p.referencia === elegida.referencia)) {
          this.seleccionada.set(null);
        }
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(mensajeError(err, 'No pudimos cargar las boletas en reventa'));
        this.cargando.set(false);
      },
    });
  }

  protected elegir(publicacion: PublicacionReventa): void {
    if (this.sesion() == null) {
      this.pendiente.set({
        nombre: `${publicacion.nombreZona} · ${publicacion.categoriaEntrada}`,
        precio: publicacion.precio,
      });
      this.dialogoSesion()?.abrir();
      return;
    }

    if (!this.esUsuario()) {
      this.toastService.error('No disponible', 'Solo una cuenta de usuario puede comprar boletas.');
      return;
    }

    this.seleccionada.set(publicacion);
    this.metodo = '';
    this.errorMetodo.set('');
    this.errorCompra.set('');

    setTimeout(() => {
      const reducir = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
      this.panelCompra()?.nativeElement.scrollIntoView({
        behavior: reducir ? 'auto' : 'smooth',
        block: 'center',
      });
    });
  }

  protected cancelar(): void {
    this.seleccionada.set(null);
  }

  protected comprar(): void {
    const publicacion = this.seleccionada();

    this.errorCompra.set('');
    this.errorMetodo.set(this.metodo ? '' : 'Selecciona un método de pago');

    if (publicacion == null || !this.metodo || this.enviando()) {
      return;
    }

    this.enviando.set(true);

    this.publicacionService.comprar(publicacion.referencia, this.metodo).subscribe({
      next: () => {
        this.enviando.set(false);
        this.toastService.success('Boleta comprada', 'Ya está en Mis boletas con su código QR.');
        this.router.navigate(['/app/usuario/boletas']);
      },
      error: (err: HttpErrorResponse) => {
        this.enviando.set(false);
        this.errorCompra.set(mensajeError(err, 'No se pudo comprar la boleta'));
        this.cargar();
      },
    });
  }

  protected irAIngresar(): void {
    this.router.navigate(['/acceso'], {
      queryParams: { volver: `/eventos/${this.idFestival()}?pestana=reventa` },
    });
  }

  protected irARegistro(): void {
    this.router.navigate(['/registro']);
  }
}
