import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';

type MenuItem = {
  label: string;
  icon?: string;
  link?: string;
  isHeader?: boolean;
};

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  menuItems: MenuItem[] = [
    { label: 'Dashboard', icon: 'dashboard', link: '/dashboard' },
    { label: 'Jobs de Backup', icon: 'schedule', link: '/jobs' },
    
    { label: 'Configurações', isHeader: true }, 
    
    { label: 'Fontes de Dados', icon: 'dns', link: '/sources' },
    { label: 'Destinos', icon: 'storage', link: '/destinations' },
    { label: 'Notificações (Email)', icon: 'mail', link: '/settings/email' },
    { label: 'Usuários', icon: 'group', link: '/settings/users' },
  ];

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}