import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { filter, map, scan } from 'rxjs';

import { ETIQUETA_ROL, TipoUsuario } from '../../models/modelos';
import { AuthService } from '../../services/auth.service';
import { ListaEsperaService } from '../../services/lista-espera.service';
import { PublicacionReventaService } from '../../services/publicacion-reventa.service';
import { ThemeService } from '../../services/theme.service';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog';

interface OpcionMenu {
  ruta: string;
  etiqueta: string;
  icono: string;
}

const GESTION: OpcionMenu[] = [
  { ruta: '/app/gestion/festivales', etiqueta: 'Festivales', icono: 'pi-calendar' },
  { ruta: '/app/gestion/artistas', etiqueta: 'Artistas', icono: 'pi-microphone' },
  { ruta: '/app/gestion/patrocinadores', etiqueta: 'Patrocinadores', icono: 'pi-briefcase' },
  { ruta: '/app/gestion/acceso', etiqueta: 'Control de acceso', icono: 'pi-qrcode' },
];

const MENU_REVENDEDOR: OpcionMenu[] = [
  { ruta: '/app/revendedor/boletas', etiqueta: 'Revender boletas', icono: 'pi-ticket' },
  { ruta: '/app/revendedor/ventas', etiqueta: 'Mis ventas', icono: 'pi-wallet' },
];

type VistaUsuario = 'usuario' | 'revendedor';

// Perfil o notificaciones son comunes a las dos vistas: ahí se conserva la que venía.
function vistaDeRuta(actual: VistaUsuario, url: string): VistaUsuario {
  if (url.startsWith('/app/revendedor')) {
    return 'revendedor';
  }

  return url.startsWith('/app/usuario') ? 'usuario' : actual;
}

const MENU: Record<TipoUsuario, OpcionMenu[]> = {
  USUARIO: [
    { ruta: '/app/usuario/boletas', etiqueta: 'Mis boletas', icono: 'pi-ticket' },
    { ruta: '/app/usuario/historial', etiqueta: 'Historial', icono: 'pi-receipt' },
    { ruta: '/app/usuario/listas', etiqueta: 'Listas de espera', icono: 'pi-clock' },
  ],
  ORGANIZADOR: GESTION,
  ADMINISTRADOR: [
    { ruta: '/app/admin/dashboard', etiqueta: 'Dashboard', icono: 'pi-chart-line' },
    ...GESTION,
    { ruta: '/app/admin/usuarios', etiqueta: 'Usuarios', icono: 'pi-users' },
    { ruta: '/app/admin/auditoria', etiqueta: 'Auditoría', icono: 'pi-shield' },
  ],
};

@Component({
  selector: 'app-sidebar',
  imports: [ConfirmDialogComponent, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class SidebarComponent implements OnInit {
  private authService = inject(AuthService);
  private listaService = inject(ListaEsperaService);
  private publicacionService = inject(PublicacionReventaService);
  private router = inject(Router);
  protected themeService = inject(ThemeService);

  protected confirmandoSalida = signal(false);
  private avisos = signal(0);
  protected totalNotificaciones = computed(
    () => this.avisos() + this.publicacionService.vendidas(),
  );

  private sesion = this.authService.sesion;

  protected esUsuario = computed(() => this.sesion()?.tipoUsuario === 'USUARIO');
  protected vista = toSignal(
    this.router.events.pipe(
      filter((evento) => evento instanceof NavigationEnd),
      map((evento) => evento.urlAfterRedirects),
      scan(vistaDeRuta, vistaDeRuta('usuario', this.router.url)),
    ),
    { initialValue: vistaDeRuta('usuario', this.router.url) },
  );
  private enVistaRevendedor = computed(() => this.esUsuario() && this.vista() === 'revendedor');

  protected rol = computed(() =>
    this.enVistaRevendedor() ? 'Revendedor' : ETIQUETA_ROL[this.sesion()?.tipoUsuario ?? 'USUARIO'],
  );
  protected opciones = computed(() =>
    this.enVistaRevendedor() ? MENU_REVENDEDOR : MENU[this.sesion()?.tipoUsuario ?? 'USUARIO'],
  );
  protected nombreUsuario = computed(() => this.sesion()?.nombreUsuario ?? 'Usuario');
  protected iniciales = computed(() => this.nombreUsuario().slice(0, 2).toUpperCase());

  ngOnInit(): void {
    const documento = this.sesion()?.documento;

    if (documento != null) {
      // Un fallo aquí no debe molestar: la pantalla de notificaciones muestra su propio error
      this.listaService.notificaciones(documento).subscribe({
        next: (lista) => this.avisos.set(lista.length),
        error: () => this.avisos.set(0),
      });
      this.publicacionService.ventas(documento).subscribe({ error: () => undefined });
    }
  }

  protected cerrarSesion(): void {
    this.confirmandoSalida.set(false);
    this.authService.cerrarSesion();
    this.router.navigate(['']);
  }
}
