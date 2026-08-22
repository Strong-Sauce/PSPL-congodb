import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { AuthService } from '../services/auth.service';

export const guestGuard: CanActivateFn = (): Observable<boolean | UrlTree> => {

  const authService = inject(AuthService);
  const router = inject(Router);

  /*
   * Already authenticated locally.
   * Don't call /me.
   */
  if (authService.currentUser() !== null) {
    return of(router.createUrlTree(['/']));
  }

  /*
   * Check whether a backend session exists.
   *
   * fetchCurrentUser() is shared/cached by AuthService,
   * so this won't create another /me request if authGuard
   * is already doing one.
   */
  return authService.fetchCurrentUser().pipe(

    map(user => {
      if (user !== null) {
        return router.createUrlTree(['/']);
      }

      return true;
    }),

    catchError(() => {
      /*
       * If /me fails, treat the visitor as logged out
       * and allow access to guest pages.
       */
      return of(true);
    })
  );
};
