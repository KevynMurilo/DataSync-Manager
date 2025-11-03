import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { BackupService } from '../../core/services/backup.service';
import { BackupStatus } from '../../core/models/enums';
import { Observable } from 'rxjs';
import { Page } from '../../core/models/api-dtos';
import { DashboardService } from '../../core/services/dashboard.service';
import { DashboardData } from '../../core/models/dashboard.model';
import { BackupRecord } from '../../core/models/backup-record.model';
import { ModalComponent } from '../../shared/modal/modal.component';
import { LogViewerComponent } from '../../shared/log-viewer/log-viewer.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule, 
    DatePipe, 
    DecimalPipe,
    ModalComponent,
    LogViewerComponent
  ],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  private dashboardService = inject(DashboardService);
  private backupService = inject(BackupService);

  data$!: Observable<DashboardData>;
  historyPage$!: Observable<Page<BackupRecord>>;
  BackupStatus = BackupStatus;

  currentPage = 0;
  pageSize = 5;

  isLogModalVisible = false;
  selectedJobIdForLogs: string | undefined = undefined;

  ngOnInit(): void {
    this.loadDashboard();
    this.loadHistory();
  }

  loadDashboard(): void {
    this.data$ = this.dashboardService.getDashboardData();
  }

  loadHistory(page: number = 0): void {
    this.currentPage = page;
    this.historyPage$ = this.backupService.getHistory(this.currentPage, this.pageSize);
  }

  restore(id: string): void {
    if (confirm('Tem certeza que deseja restaurar este backup?')) {
      this.backupService.restoreBackup(id).subscribe(() => {
        alert('Restauração iniciada em segundo plano.');
        this.loadDashboard();
        this.loadHistory();
      });
    }
  }

  formatBytes(bytes: number, decimals = 2): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
  }

  openLogModal(jobId: string | undefined): void {
    if (!jobId) {
      alert("Erro: ID do Job não encontrado.");
      return;
    }
    this.selectedJobIdForLogs = jobId;
    this.isLogModalVisible = true;
  }

  closeLogModal(): void {
    this.isLogModalVisible = false;
    this.selectedJobIdForLogs = undefined;
  }
}