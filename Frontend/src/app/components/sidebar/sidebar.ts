import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

import { ETIQUETA_ROL, TipoUsuario } from '../../models/modelos';
import { AuthService } from '../../services/auth.service';
import { ListaEsperaService } from '../../services/lista-espera.service';
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
  { ruta: '/app/gestion/acceso', etiqueta: 'Control de acceso', icono: 'pi-qrcode' },
];

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
  private router = inject(Router);
  protected themeService = inject(ThemeService);

  protected confirmandoSalida = signal(false);
  protected totalNotificaciones = signal(0);

  private sesion = this.authService.sesion;

  protected rol = computed(() => ETIQUETA_ROL[this.sesion()?.tipoUsuario ?? 'USUARIO']);
  protected opciones = computed(() => MENU[this.sesion()?.tipoUsuario ?? 'USUARIO']);
  protected nombreUsuario = computed(() => this.sesion()?.nombreUsuario ?? 'Usuario');
  protected iniciales = computed(() => this.nombreUsuario().slice(0, 2).toUpperCase());

  ngOnInit(): void {
    const documento = this.sesion()?.documento;

    if (documento != null) {
      // Un fallo aquí no debe molestar: la pantalla de notificaciones muestra su propio error
      this.listaService.notificaciones(documento).subscribe({
        next: (lista) => this.totalNotificaciones.set(lista.length),
        error: () => this.totalNotificaciones.set(0),
      });
    }
  }

  protected cerrarSesion(): void {
    this.confirmandoSalida.set(false);
    this.authService.cerrarSesion();
    this.router.navigate(['']);
  }
}
