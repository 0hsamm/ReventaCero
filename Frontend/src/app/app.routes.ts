import { Routes } from '@angular/router';

import { MainLayoutComponent } from './components/layout/main-layout/main-layout';
import { Login } from './components/login/login';
import { authGuard } from './guards/auth-guard-guard';

export const routes: Routes = [
  {
    path: '',
    component: Login,
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
