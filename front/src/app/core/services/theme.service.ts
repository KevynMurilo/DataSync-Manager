import { Injectable, signal, WritableSignal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly THEME_KEY = 'backup-theme';
  public isDarkMode: WritableSignal<boolean> = signal(false);

  constructor() {
    this.initTheme();
  }

  private initTheme(): void {
    const theme = this.getTheme();
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    
    if (theme === 'dark' || (!theme && prefersDark)) {
      this.setDarkTheme();
    } else {
      this.setLightTheme();
    }
  }

  toggleTheme(): void {
    if (this.isDarkMode()) { 
      this.setLightTheme();
    } else {
      this.setDarkTheme();
    }
  }

  private setDarkTheme(): void {
    document.documentElement.classList.add('dark');
    localStorage.setItem(this.THEME_KEY, 'dark');
    this.isDarkMode.set(true); 
  }

  private setLightTheme(): void {
    document.documentElement.classList.remove('dark');
    localStorage.setItem(this.THEME_KEY, 'light');
    this.isDarkMode.set(false); 
  }

  private getTheme(): string | null {
    return localStorage.getItem(this.THEME_KEY);
  }
}