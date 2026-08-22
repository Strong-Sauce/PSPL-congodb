import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map, shareReplay, tap } from 'rxjs/operators';

import {
  User,
  LoginRequest,
  SignupRequest,
  AuthResponse,
} from '../models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly http = inject(HttpClient);

  private readonly baseUrl = 'http://localhost:8080/api/auth';

  // ============================================================
  // AUTH STATE
  // ============================================================

  private readonly _currentUser = signal<User | null>(null);

  readonly currentUser = this._currentUser.asReadonly();

  readonly isAuthenticated = computed(
    () => this._currentUser() !== null
  );


  // ============================================================
  // SHARED SESSION REQUEST
  // ============================================================

  /*
   * Holds the currently running /me request.
   *
   * This is important because App + authGuard + guestGuard
   * can all ask for the current user during application startup.
   *
   * Without sharing, each caller would create another HTTP request.
   */
  private currentUserRequest$: Observable<User | null> | null = null;


  // ============================================================
  // SESSION RESTORE
  // ============================================================

  /**
   * Restores the current session when the Angular application starts.
   *
   * App calls this once during startup.
   *
   * The request is shared with authGuard / guestGuard through
   * currentUserRequest$, so startup does NOT create duplicate /me calls.
   */
  initSession(): void {
    this.fetchCurrentUser().subscribe({
      error: () => {
        // fetchCurrentUser already converts failures to null.
      }
    });
  }


  // ============================================================
  // CURRENT USER
  // ============================================================

  /**
   * Returns the currently authenticated user.
   *
   * Behaviour:
   *
   * 1. User already known locally
   *    -> return immediately.
   *
   * 2. /me request already running
   *    -> reuse the same request.
   *
   * 3. Otherwise
   *    -> make exactly one /me request.
   */
  fetchCurrentUser(): Observable<User | null> {

    // ------------------------------------------------------------
    // Already authenticated locally
    // ------------------------------------------------------------

    const user = this.currentUser();

    if (user !== null) {
      return of(user);
    }


    // ------------------------------------------------------------
    // A /me request is already running
    // ------------------------------------------------------------

    if (this.currentUserRequest$ !== null) {
      return this.currentUserRequest$;
    }


    // ------------------------------------------------------------
    // Create the single shared /me request
    // ------------------------------------------------------------

    this.currentUserRequest$ = this.http
      .get<User>(`${this.baseUrl}/me`, {
        withCredentials: true
      })
      .pipe(

        // Successful session restore
        tap(user => {
          this._currentUser.set(user);
        }),

        // No session / backend unavailable
        catchError(err => {

          console.error(
            'Failed to restore authentication session:',
            err
          );

          this._currentUser.set(null);

          return of(null);
        }),

        /*
         * Critical:
         *
         * Multiple subscribers receive the same HTTP response.
         * Therefore:
         *
         * App
         * authGuard
         * guestGuard
         *
         * can all subscribe without generating multiple /me requests.
         */
        shareReplay({
          bufferSize: 1,
          refCount: false
        })
      );

    return this.currentUserRequest$;
  }


  // ============================================================
  // LOGIN
  // ============================================================

  /**
   * Logs the user in.
   *
   * Backend response:
   *
   * {
   *   message: "Login successful",
   *   user: {
   *     id,
   *     name,
   *     email
   *   }
   * }
   *
   * The component only needs the User object, so this method
   * maps AuthResponse -> User.
   */
  login(request: LoginRequest): Observable<User> {

    return this.http
      .post<AuthResponse>(
        `${this.baseUrl}/login`,
        request,
        {
          withCredentials: true
        }
      )
      .pipe(

        map(response => response.user),

        tap(user => {
          this.setCurrentUser(user);
        })
      );
  }


  // ============================================================
  // SIGNUP
  // ============================================================

  /**
   * Registers a new user.
   *
   * Backend automatically creates the session after signup.
   *
   * Therefore we immediately store response.user as the
   * authenticated frontend user.
   */
  signup(request: SignupRequest): Observable<User> {

    return this.http
      .post<AuthResponse>(
        `${this.baseUrl}/signup`,
        request,
        {
          withCredentials: true
        }
      )
      .pipe(

        map(response => response.user),

        tap(user => {
          this.setCurrentUser(user);
        })
      );
  }


  // ============================================================
  // SET CURRENT USER
  // ============================================================

  /**
   * Updates the frontend authentication state.
   *
   * Also prevents another /me call because the authenticated
   * user is now already known.
   */
  setCurrentUser(user: User): void {

    this._currentUser.set(user);

    this.currentUserRequest$ = of(user);
  }


  // ============================================================
  // LOGOUT
  // ============================================================

  /**
   * Logs out from the backend and clears local authentication.
   */
  logout(): Observable<void> {

    return this.http
      .post<void>(
        `${this.baseUrl}/logout`,
        {},
        {
          withCredentials: true
        }
      )
      .pipe(

        tap(() => {
          this.clearCurrentUser();
        }),

        /*
         * Even if the backend logout fails, the frontend should
         * not remain in an authenticated state.
         */
        catchError(err => {

          console.error(
            'Logout request failed:',
            err
          );

          this.clearCurrentUser();

          return of(void 0);
        })
      );
  }


  // ============================================================
  // CLEAR AUTH STATE
  // ============================================================

  clearCurrentUser(): void {

    this._currentUser.set(null);

    this.currentUserRequest$ = null;
  }
}
