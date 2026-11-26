import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/recipes/pages/home/home').then((m) => m.Home),
  },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'admin', loadChildren: () => import('./features/admin/admin.routes').then((m) => m.adminRoutes) },
  { path: '**', redirectTo: '' },
];