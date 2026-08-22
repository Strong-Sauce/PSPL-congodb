import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (): Observable<boolean | ReturnType<Router['createUrlTree']>> => {

  const authService = inject(AuthService);
  const router = inject(Router);

  /*
   * If the user is already known locally,
   * don't make another /me request.
   */
  if (authService.currentUser() !== null) {
    return of(true);
  }

  /*
   * Otherwise restore/check the session.
   *
   * AuthService handles request sharing, so if another
   * guard is also checking authentication, it will reuse
   * the same /me request.
   */
  return authService.fetchCurrentUser().pipe(

    map(user => {
      return user !== null ? true : router.createUrlTree(['/login']);
    }),

    catchError(() => {
      return of(router.createUrlTree(['/login']));
    })
  );
};
