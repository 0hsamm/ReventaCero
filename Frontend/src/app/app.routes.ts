import { inject } from '@angular/core';
import { Routes } from '@angular/router';

import { AdminAuditoriaComponent } from './components/admin/auditoria/auditoria';
import { AdminDashboardComponent } from './components/admin/dashboard/dashboard';
import { AdminUsuariosComponent } from './components/admin/usuarios/usuarios';
import { CatalogoComponent } from './components/catalogo/catalogo';
import { EventoDetalleComponent } from './components/evento-detalle/evento-detalle';
import { GestionArtistasComponent } from './components/gestion/artistas/artistas';
import { ControlAccesoComponent } from './components/gestion/control-acceso/control-acceso';
import { FestivalConfigComponent } from './components/gestion/festival-config/festival-config';
import { GestionFestivalesComponent } from './components/gestion/festivales/festivales';
import { GestionPatrocinadoresComponent } from './components/gestion/patrocinadores/patrocinadores';
import { LandingComponent } from './components/landing/landing';
import { MainLayoutComponent } from './components/layout/main-layout/main-layout';
import { Login } from './components/login/login';
import { NoEncontradoComponent } from './components/no-encontrado/no-encontrado';
import { NotificacionesComponent } from './components/notificaciones/notificaciones';
import { PerfilComponent } from './components/perfil/perfil';
import { PublicLayoutComponent } from './components/public-layout/public-layout';
import { RegistroComponent } from './components/registro/registro';
import { RevenderComponent } from './components/revendedor/revender/revender';
import { VentasRevendedorComponent } from './components/revendedor/ventas/ventas';
import { BoletasReventaComponent } from './components/usuario/boletas-reventa/boletas-reventa';
import { HistorialComponent } from './components/usuario/historial/historial';
import { ListasEsperaComponent } from './components/usuario/listas-espera/listas-espera';
import { MisBoletasComponent } from './components/usuario/mis-boletas/mis-boletas';
import { authGuard, rolGuard } from './guards/auth-guard-guard';
import { AuthService } from './services/auth.service';

export const routes: Routes = [
  { path: 'acceso', component: Login },
  { path: 'registro', component: RegistroComponent },
  {
    path: 'app',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    canActivateChild: [authGuard],
    children: [
      { path: 'notificaciones', component: NotificacionesComponent },
      { path: 'perfil', component: PerfilComponent },
      {
        path: 'usuario',
        canActivate: [rolGuard('USUARIO')],
        canActivateChild: [rolGuard('USUARIO')],
        children: [
          { path: 'boletas', component: MisBoletasComponent },
          { path: 'reventas', component: BoletasReventaComponent },
          { path: 'historial', component: HistorialComponent },
          { path: 'listas', component: ListasEsperaComponent },
          { path: '', redirectTo: 'boletas', pathMatch: 'full' },
        ],
      },
      // El backend no tiene un rol revendedor: es otra vista de la misma cuenta de usuario
      {
        path: 'revendedor',
        canActivate: [rolGuard('USUARIO')],
        canActivateChild: [rolGuard('USUARIO')],
        children: [
          { path: 'boletas', component: RevenderComponent },
          { path: 'ventas', component: VentasRevendedorComponent },
          { path: '', redirectTo: 'boletas', pathMatch: 'full' },
        ],
      },
      {
        path: 'gestion',
        canActivate: [rolGuard('ADMINISTRADOR', 'ORGANIZADOR')],
        canActivateChild: [rolGuard('ADMINISTRADOR', 'ORGANIZADOR')],
        children: [
          { path: 'festivales', component: GestionFestivalesComponent },
          { path: 'festivales/:id', component: FestivalConfigComponent },
          { path: 'artistas', component: GestionArtistasComponent },
          { path: 'patrocinadores', component: GestionPatrocinadoresComponent },
          { path: 'acceso', component: ControlAccesoComponent },
          { path: '', redirectTo: 'festivales', pathMatch: 'full' },
        ],
      },
      {
        path: 'admin',
        canActivate: [rolGuard('ADMINISTRADOR')],
        canActivateChild: [rolGuard('ADMINISTRADOR')],
        children: [
          { path: 'dashboard', component: AdminDashboardComponent },
          { path: 'usuarios', component: AdminUsuariosComponent },
          { path: 'auditoria', component: AdminAuditoriaComponent },
          { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
        ],
      },
      { path: '', redirectTo: () => inject(AuthService).rutaInicio(), pathMatch: 'full' },
    ],
  },
  {
    path: '',
    component: PublicLayoutComponent,
    children: [
      { path: '', component: LandingComponent, pathMatch: 'full' },
      { path: 'eventos', component: CatalogoComponent },
      { path: 'eventos/:id', component: EventoDetalleComponent },
      { path: '**', component: NoEncontradoComponent },
    ],
  },
];
