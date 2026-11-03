import { Routes } from '@angular/router';

import { LandingComponent } from './components/landing/landing';
import { MainLayoutComponent } from './components/layout/main-layout/main-layout';
import { LegalComponent } from './components/legal/legal';
import { Login } from './components/login/login';
import { authGuard } from './guards/auth-guard-guard';

export const routes: Routes = [
  {
    path: '',
    component: LandingComponent,
  },
  {
    path: 'acceso',
    component: Login,
  },
  {
    path: 'legal/:documento',
    component: LegalComponent,
  },
  {
    path: 'app',
    component: MainLayoutComponent,
    canActivate: [authGuard],
  },
  {
    path: '**',
    redirectTo: '',
  },
];
