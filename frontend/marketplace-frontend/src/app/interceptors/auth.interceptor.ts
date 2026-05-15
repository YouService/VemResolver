import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('token');
  const router = inject(Router);
  const authService = inject(AuthService);

  const authReq = token
    ? req.clone({ headers: req.headers.set('Authorization', `Bearer ${token}`) })
    : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !req.url.includes('/api/auth/')) {
        const refreshToken = localStorage.getItem('refreshToken');
        if (refreshToken) {
          return authService.refresh(refreshToken).pipe(
            switchMap(response => {
              const retryReq = req.clone({
                headers: req.headers.set('Authorization', `Bearer ${response.token}`)
              });
              return next(retryReq);
            }),
            catchError(() => {
              authService.logout();
              router.navigate(['/login']);
              return throwError(() => error);
            })
          );
        }
        authService.logout();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
