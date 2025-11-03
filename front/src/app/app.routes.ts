import { Routes } from '@angular/router';
import { LayoutComponent } from './layout/layout.component';
import { authGuard } from './core/guards/auth.guard';
import { forceChangePasswordGuard } from './core/guards/force-change-password.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'force-change-password',
    loadComponent: () => import('./pages/force-change-password/force-change-password.component').then(m => m.ForceChangePasswordComponent),
    canActivate: [authGuard]
  },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard, forceChangePasswordGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'sources',
        loadComponent: () => import('./pages/sources/source-list/source-list.component').then(m => m.SourceListComponent)
      },
      {
        path: 'jobs',
        loadComponent: () => import('./pages/jobs/job-list/job-list.component').then(m => m.JobListComponent)
      },
      {
        path: 'destinations',
        loadComponent: () => import('./pages/destinations/destination-list/destination-list.component').then(m => m.DestinationListComponent)
      },
      {
        path: 'settings/email',
        loadComponent: () => import('./pages/settings/email-config-list/email-config-list.component').then(m => m.EmailConfigListComponent)
      },
      {
        path: 'settings/users',
        loadComponent: () => import('./pages/settings/user-list/user-list.component').then(m => m.UserListComponent)
      },
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      }
    ]
  },
  { path: '**', redirectTo: 'login' }
];