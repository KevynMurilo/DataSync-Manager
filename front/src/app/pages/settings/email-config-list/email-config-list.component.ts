import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmailConfig } from '../../../core/models/email-config.model';
import { EmailConfigService } from '../../../core/services/email-config.service';
import { Observable } from 'rxjs';
import { ModalComponent } from '../../../shared/modal/modal.component';
import { EmailConfigFormComponent } from '../email-config-form/email-config-form.component';

@Component({
  selector: 'app-email-config-list',
  standalone: true,
  imports: [
    CommonModule,
    ModalComponent,
    EmailConfigFormComponent
  ],
  templateUrl: './email-config-list.component.html'
})
export class EmailConfigListComponent implements OnInit {
  private emailConfigService = inject(EmailConfigService);
  
  configs$!: Observable<EmailConfig[]>;

  isModalVisible = false;
  selectedConfigId: string | undefined = undefined;

  ngOnInit(): void {
    this.loadConfigs();
  }

  loadConfigs(): void {
    this.configs$ = this.emailConfigService.findAll();
  }

  delete(id: string, name: string): void {
    if (confirm(`Tem certeza que deseja excluir a configuração "${name}"?`)) {
      this.emailConfigService.deleteById(id).subscribe(() => {
        this.loadConfigs();
      });
    }
  }

  openModal(id?: string): void {
    this.selectedConfigId = id;
    this.isModalVisible = true;
  }

  closeModal(): void {
    this.isModalVisible = false;
    this.selectedConfigId = undefined;
  }

  onFormSaved(): void {
    this.loadConfigs();
    this.closeModal();
  }
}