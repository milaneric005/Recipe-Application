import { Routes } from '@angular/router';

export const adminRoutes: Routes = [
  { path: '', redirectTo: 'types', pathMatch: 'full' },
  {
    path: 'types',
    loadComponent: () => import('./pages/admin-types/admin-types').then((m) => m.AdminTypes),
  },
];
