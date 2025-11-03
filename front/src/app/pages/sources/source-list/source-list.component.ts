import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BackupSourceService } from '../../../core/services/backup-source.service';
import { BackupSource } from '../../../core/models/backup-source.model';
import { Observable } from 'rxjs';
import { SourceFormComponent } from '../source-form/source-form.component';
import { ModalComponent } from '../../../shared/modal/modal.component';

@Component({
  selector: 'app-source-list',
  standalone: true,
  imports: [
    CommonModule, 
    ModalComponent,
    SourceFormComponent
  ],
  templateUrl: './source-list.component.html'
})
export class SourceListComponent implements OnInit {
  private sourceService = inject(BackupSourceService);
  
  sources$!: Observable<BackupSource[]>;

  isModalVisible = false;
  selectedSourceId: string | undefined = undefined;

  ngOnInit(): void {
    this.loadSources();
  }

  loadSources(): void {
    this.sources$ = this.sourceService.findAll();
  }

  delete(id: string, name: string): void {
    if (confirm(`Tem certeza que deseja excluir a fonte de dados "${name}"?`)) {
      this.sourceService.deleteById(id).subscribe(() => {
        this.loadSources();
      });
    }
  }

  openModal(id?: string): void {
    this.selectedSourceId = id;
    this.isModalVisible = true;
  }

  closeModal(): void {
    this.isModalVisible = false;
    this.selectedSourceId = undefined;
  }

  onFormSaved(): void {
    this.loadSources();
    this.closeModal();
  }
}
