import { Component, inject } from '@angular/core';
import { ThemeService } from '../../core/services/theme.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header.component.html'
})
export class HeaderComponent {
  public themeService = inject(ThemeService);

  toggleTheme(): void {
    this.themeService.toggleTheme();
  }
}