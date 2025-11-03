import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EmailConfig, EmailConfigDTO } from '../models/email-config.model';

@Injectable({
  providedIn: 'root'
})
export class EmailConfigService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8082/api/email-configs';

  findAll(): Observable<EmailConfig[]> {
    return this.http.get<EmailConfig[]>(this.apiUrl);
  }

  findById(id: string): Observable<EmailConfig> {
    return this.http.get<EmailConfig>(`${this.apiUrl}/${id}`);
  }

  save(dto: EmailConfigDTO): Observable<EmailConfig> {
    return this.http.post<EmailConfig>(this.apiUrl, dto);
  }

  update(id: string, dto: EmailConfigDTO): Observable<EmailConfig> {
    return this.http.put<EmailConfig>(`${this.apiUrl}/${id}`, dto);
  }

  deleteById(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  testConnection(dto: EmailConfigDTO): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/test`, dto);
  }
}