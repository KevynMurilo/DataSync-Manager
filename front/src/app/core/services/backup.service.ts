import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BackupRecord } from '../models/backup-record.model';
import { Page } from '../models/api-dtos';

@Injectable({
  providedIn: 'root'
})
export class BackupService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8082/api/backup';

  getHistory(page: number, size: number): Observable<Page<BackupRecord>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'timestamp,desc');
      
    return this.http.get<Page<BackupRecord>>(`${this.apiUrl}/history`, { params });
  }

  restoreBackup(recordId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/restore/${recordId}`, {});
  }
}