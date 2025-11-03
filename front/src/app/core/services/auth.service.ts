import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ChangePasswordDTO, JwtResponse, LoginRequest } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8082/api/auth';
  private readonly TOKEN_KEY = 'auth_token';
  private readonly CHANGE_PASS_KEY = 'must_change_password';

  login(dto: LoginRequest): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(`${this.apiUrl}/login`, dto).pipe(
      tap(response => this.saveSession(response))
    );
  }

  changePassword(dto: ChangePasswordDTO): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(`${this.apiUrl}/change-password`, dto).pipe(
      tap(response => this.saveSession(response))
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.CHANGE_PASS_KEY);
  }

  private saveSession(response: JwtResponse): void {
    this.saveToken(response.token);
    if (response.mustChangePassword) {
      localStorage.setItem(this.CHANGE_PASS_KEY, 'true');
    } else {
      localStorage.removeItem(this.CHANGE_PASS_KEY);
    }
  }

  private saveToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return this.getToken() !== null;
  }
  
  isPasswordChangeRequired(): boolean {
    return localStorage.getItem(this.CHANGE_PASS_KEY) === 'true';
  }
}