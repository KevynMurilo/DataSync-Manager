import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BackupJob, BackupJobDTO } from '../models/backup-job.model';

@Injectable({
  providedIn: 'root'
})
export class BackupJobService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8082/api/backup-jobs';

  findAll(): Observable<BackupJob[]> {
    return this.http.get<BackupJob[]>(this.apiUrl);
  }

  findById(id: string): Observable<BackupJob> {
    return this.http.get<BackupJob>(`${this.apiUrl}/${id}`);
  }

  save(dto: BackupJobDTO): Observable<BackupJob> {
    return this.http.post<BackupJob>(this.apiUrl, dto);
  }

  update(id: string, dto: BackupJobDTO): Observable<BackupJob> {
    return this.http.put<BackupJob>(`${this.apiUrl}/${id}`, dto);
  }

  deleteById(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  execute(id: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${id}/execute`, {});
  }
}