import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { guestGuard } from './guards/guest.guard';

export const routes: Routes = [
  // ─── AUTH ROUTES (guests only) ───────────────────────────────────────────
  // guestGuard redirects already-logged-in users to '/' so they don't see login again

  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./pages/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'signup',
    canActivate: [guestGuard],
    loadComponent: () => import('./pages/signup/signup.component').then((m) => m.SignupComponent),
  },

  // ─── PROTECTED ROUTES (login required) ────────────────────────────────────
  // authGuard redirects unauthenticated users to '/login'

  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/home/home.component').then((m) => m.HomeComponent),
  },
  {
    path: 'purchases/new',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/purchase-create/purchase-create.component').then(
        (m) => m.PurchaseCreateComponent,
      ),
  },
  {
    path: 'products/:productSerialNumber',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/product-detail/product-detail.component').then(
        (m) => m.ProductDetailComponent,
      ),
  },
  {
    path: 'warranties/:warrantyId',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/warranty-detail/warranty-detail.component').then(
        (m) => m.WarrantyDetailComponent,
      ),
  },
  {
    path: 'about',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/about/about.component').then((m) => m.AboutComponent),
  },
  {
    path: 'contact',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/contact/contact.component').then((m) => m.ContactComponent),
  },
  {
    path: 'profile',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/profile/profile.component').then((m) => m.ProfileComponent),
  },

  { path: '**', redirectTo: '' },
];
