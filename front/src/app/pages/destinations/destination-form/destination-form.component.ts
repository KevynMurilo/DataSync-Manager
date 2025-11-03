import { Component, Input, Output, EventEmitter, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DestinationService } from '../../../core/services/destination.service';
import { BackupType } from '../../../core/models/enums';
import { BackupDestinationDTO } from '../../../core/models/backup-destination.model';

@Component({
  selector: 'app-destination-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './destination-form.component.html'
})
export class DestinationFormComponent implements OnInit {
  @Input() id?: string;
  @Output() onSaveSuccess = new EventEmitter<void>();
  @Output() onCancel = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private destinationService = inject(DestinationService);

  destinationForm: FormGroup;
  backupTypes = Object.values(BackupType);
  isEditMode = false;

  isTesting = false;
  testMessage: string | null = null;
  isTestSuccess = false;

  BackupType = BackupType;

  constructor() {
    this.destinationForm = this.fb.group({
      name: ['', Validators.required],
      type: [null, Validators.required],
      endpoint: ['', Validators.required],
      region: [''],
      accessKey: [''],
      secretKey: [''],
      isActive: [true, Validators.required]
    });
  }

  ngOnInit(): void {
    if (this.id) {
      this.isEditMode = true;
      this.destinationService.findById(this.id).subscribe(dest => {
        this.destinationForm.patchValue(dest);
      });
    }
  }

  get f() {
    return this.destinationForm.controls;
  }

  get selectedType(): BackupType | null {
    return this.destinationForm.get('type')?.value;
  }

  onTestConnection(): void {
    if (this.destinationForm.invalid) {
      this.isTestSuccess = false;
      this.testMessage = "Formulário inválido. Preencha os campos obrigatórios para testar.";
      return;
    }

    this.isTesting = true;
    this.testMessage = null;
    this.isTestSuccess = false;

    const dto: BackupDestinationDTO = this.destinationForm.value;

    this.destinationService.testConnection(dto).subscribe({
      next: (response) => {
        this.isTesting = false;
        this.isTestSuccess = true;
        this.testMessage = response.message || 'Conexão bem-sucedida!';
      },
      error: (err) => {
        this.isTesting = false;
        this.isTestSuccess = false;
        this.testMessage = err.error?.message || 'Falha ao conectar. Verifique os dados.';
      }
    });
  }

  saveDestination(): void {
    if (this.destinationForm.invalid) {
      alert('Formulário inválido');
      return;
    }

    const dto: BackupDestinationDTO = this.destinationForm.value;

    if (!this.isEditMode) {
      if (dto.type !== BackupType.LOCAL_DISK && (!dto.accessKey || !dto.secretKey)) {
        alert('Destinos de Cloud/FTP requerem Access Key e Secret Key.');
        return;
      }
    }
    
    if (this.isEditMode && dto.type !== BackupType.LOCAL_DISK) {
      if (!dto.accessKey) delete dto.accessKey;
      if (!dto.secretKey) delete dto.secretKey;
    }

    const save$ = this.isEditMode
      ? this.destinationService.update(this.id!, dto)
      : this.destinationService.save(dto);

    save$.subscribe(() => {
      this.onSaveSuccess.emit();
    });
  }

  cancel(): void {
    this.onCancel.emit();
  }
}