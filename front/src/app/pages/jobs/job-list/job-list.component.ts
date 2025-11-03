import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BackupJobService } from '../../../core/services/backup-job.service';
import { BackupJob } from '../../../core/models/backup-job.model';
import { Observable } from 'rxjs';
import { JobFormComponent } from '../job-form/job-form.component';
import { ModalComponent } from '../../../shared/modal/modal.component';

@Component({
  selector: 'app-job-list',
  standalone: true,
  imports: [
    CommonModule, 
    ModalComponent, 
    JobFormComponent
  ],
  templateUrl: './job-list.component.html'
})
export class JobListComponent implements OnInit {
  private jobService = inject(BackupJobService);
  
  jobs$!: Observable<BackupJob[]>;
  runningJobs: Set<string> = new Set();

  isModalVisible = false;
  selectedJobId: string | undefined = undefined;

  ngOnInit(): void {
    this.loadJobs();
  }

  loadJobs(): void {
    this.jobs$ = this.jobService.findAll();
  }

  delete(id: string, name: string): void {
    if (confirm(`Tem certeza que deseja excluir o job "${name}"?`)) {
      this.jobService.deleteById(id).subscribe(() => {
        this.loadJobs();
      });
    }
  }

  runNow(id: string, name: string): void {
    if (confirm(`Tem certeza que deseja executar o job "${name}" agora?`)) {
      this.runningJobs.add(id);
      this.jobService.execute(id).subscribe({
        next: () => {
          alert(`Job "${name}" iniciado com sucesso.`);
          this.runningJobs.delete(id);
        },
        error: (err) => {
          alert(`Falha ao iniciar job "${name}": ${err.message}`);
          this.runningJobs.delete(id);
        }
      });
    }
  }

  isRunning(id: string): boolean {
    return this.runningJobs.has(id);
  }

  openModal(id?: string): void {
    this.selectedJobId = id;
    this.isModalVisible = true;
  }

  closeModal(): void {
    this.isModalVisible = false;
    this.selectedJobId = undefined;
  }

  onFormSaved(): void {
    this.loadJobs();
    this.closeModal();
  }
}