import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { TipoUsuario } from '../models/modelos';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.sesion() ? true : router.createUrlTree(['/acceso']);
};

// Cada rol solo entra a sus paneles: si intenta otro, vuelve al suyo.
export function rolGuard(...roles: TipoUsuario[]): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);
    const sesion = authService.sesion();

    if (sesion == null) {
      return router.createUrlTree(['/acceso']);
    }

    return roles.includes(sesion.tipoUsuario) ? true : router.parseUrl(authService.rutaInicio());
  };
}
