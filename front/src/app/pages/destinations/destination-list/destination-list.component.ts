import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DestinationService } from '../../../core/services/destination.service';
import { BackupDestination } from '../../../core/models/backup-destination.model';
import { Observable } from 'rxjs';
import { DestinationFormComponent } from '../destination-form/destination-form.component';
import { ModalComponent } from '../../../shared/modal/modal.component';

@Component({
  selector: 'app-destination-list',
  standalone: true,
  imports: [
    CommonModule, 
    ModalComponent, 
    DestinationFormComponent
  ],
  templateUrl: './destination-list.component.html'
})
export class DestinationListComponent implements OnInit {
  private destinationService = inject(DestinationService);
  
  destinations$!: Observable<BackupDestination[]>;
  
  isModalVisible = false;
  selectedDestinationId: string | undefined = undefined;

  ngOnInit(): void {
    this.loadDestinations();
  }

  loadDestinations(): void {
    this.destinations$ = this.destinationService.findAll();
  }

  delete(id: string, name: string): void {
    if (confirm(`Tem certeza que deseja excluir o destino "${name}"?`)) {
      this.destinationService.deleteById(id).subscribe(() => {
        this.loadDestinations();
      });
    }
  }

  openModal(id?: string): void {
    this.selectedDestinationId = id;
    this.isModalVisible = true;
  }

  closeModal(): void {
    this.isModalVisible = false;
    this.selectedDestinationId = undefined;
  }

  onFormSaved(): void {
    this.loadDestinations();
    this.closeModal();
  }
}