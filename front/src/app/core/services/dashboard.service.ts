import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardData } from '../models/dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8082/api/dashboard';

  getDashboardData(): Observable<DashboardData> {
    return this.http.get<DashboardData>(this.apiUrl);
  }
}