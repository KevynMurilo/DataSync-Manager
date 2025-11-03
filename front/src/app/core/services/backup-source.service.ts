import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BackupSource, BackupSourceDTO } from '../models/backup-source.model';

@Injectable({
  providedIn: 'root'
})
export class BackupSourceService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8082/api/backup-sources';

  findAll(): Observable<BackupSource[]> {
    return this.http.get<BackupSource[]>(this.apiUrl);
  }

  findById(id: string): Observable<BackupSource> {
    return this.http.get<BackupSource>(`${this.apiUrl}/${id}`);
  }

  save(dto: BackupSourceDTO): Observable<BackupSource> {
    return this.http.post<BackupSource>(this.apiUrl, dto);
  }

  update(id: string, dto: BackupSourceDTO): Observable<BackupSource> {
    return this.http.put<BackupSource>(`${this.apiUrl}/${id}`, dto);
  }

  deleteById(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  testConnection(dto: BackupSourceDTO): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/test`, dto);
  }
}