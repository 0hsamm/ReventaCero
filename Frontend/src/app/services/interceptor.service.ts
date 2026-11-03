import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const token = authService.obtenerToken();

  const peticion = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(peticion).pipe(
    catchError((err: HttpErrorResponse) => {
      // En /auth un 401 son credenciales incorrectas, no una sesión vencida.
      if (err.status === 401 && !req.url.includes('/auth/')) {
        authService.cerrarSesion();
        router.navigate(['/acceso']);
      }

      return throwError(() => err);
    }),
  );
};
