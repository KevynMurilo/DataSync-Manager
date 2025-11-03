import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from './sidebar/sidebar.component';
import { HeaderComponent } from './header/header.component';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent, HeaderComponent],
  template: `
    <div class="flex h-screen bg-gray-100 dark:bg-gray-900">
      <app-sidebar />
      <div class="flex-1 flex flex-col overflow-hidden">
        <app-header />
        <main class="flex-1 overflow-x-hidden overflow-y-auto p-6 md:p-10">
          <router-outlet />
        </main>
      </div>
    </div>
  `
})
export class LayoutComponent {}