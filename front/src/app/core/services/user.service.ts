import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';
import { RegisterRequest } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8082/api/users';

  findAll(): Observable<User[]> {
    return this.http.get<User[]>(this.apiUrl);
  }

  createUser(dto: RegisterRequest): Observable<User> {
    return this.http.post<User>(this.apiUrl, dto);
  }

  deleteById(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}