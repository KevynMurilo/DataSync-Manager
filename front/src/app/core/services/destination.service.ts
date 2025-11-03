import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BackupDestination, BackupDestinationDTO } from '../models/backup-destination.model';

@Injectable({
  providedIn: 'root'
})
export class DestinationService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8082/api/backup-destination';

  findAll(): Observable<BackupDestination[]> {
    return this.http.get<BackupDestination[]>(this.apiUrl);
  }

  findById(id: string): Observable<BackupDestination> {
    return this.http.get<BackupDestination>(`${this.apiUrl}/${id}`);
  }

  save(dto: BackupDestinationDTO): Observable<BackupDestination> {
    return this.http.post<BackupDestination>(this.apiUrl, dto);
  }

  update(id: string, dto: BackupDestinationDTO): Observable<BackupDestination> {
    return this.http.put<BackupDestination>(`${this.apiUrl}/${id}`, dto);
  }

  deleteById(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  testConnection(dto: BackupDestinationDTO): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/test`, dto);
  }
}