import { Component, Input, Output, EventEmitter, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { EmailConfigService } from '../../../core/services/email-config.service';
import { EmailConfigDTO } from '../../../core/models/email-config.model';

@Component({
  selector: 'app-email-config-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './email-config-form.component.html'
})
export class EmailConfigFormComponent implements OnInit {
  @Input() id?: string;
  @Output() onSaveSuccess = new EventEmitter<void>();
  @Output() onCancel = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private emailConfigService = inject(EmailConfigService);

  configForm: FormGroup;
  isEditMode = false;

  isTesting = false;
  testMessage: string | null = null;
  isTestSuccess = false;

  constructor() {
    this.configForm = this.fb.group({
      name: ['', Validators.required],
      host: ['', Validators.required],
      port: [587, [Validators.required, Validators.min(1)]],
      username: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });

    this.configForm.valueChanges.subscribe(() => {
      this.testMessage = null;
    });
  }

  ngOnInit(): void {
    if (this.id) {
      this.isEditMode = true;
      this.configForm.get('password')?.setValidators(null);

      this.emailConfigService.findById(this.id).subscribe(config => {
        this.configForm.patchValue({
          name: config.name,
          host: config.host,
          port: config.port,
          username: config.username,
          password: ''
        });
      });
    }
  }

  onTestConnection(): void {
    if (this.configForm.invalid) {
      this.isTestSuccess = false;
      this.testMessage = "Formulário inválido. Preencha todos os campos para testar.";
      return;
    }

    this.isTesting = true;
    this.testMessage = null;
    this.isTestSuccess = false;

    const dto: EmailConfigDTO = this.configForm.value;
    
    this.emailConfigService.testConnection(dto).subscribe({
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

  saveConfig(): void {
    if (this.configForm.invalid) {
      alert('Formulário inválido');
      return;
    }

    const dto: EmailConfigDTO = this.configForm.value;
    
    if (this.isEditMode && !dto.password) {
      delete (dto as any).password;
    }

    const save$ = this.isEditMode
      ? this.emailConfigService.update(this.id!, dto)
      : this.emailConfigService.save(dto);

    save$.subscribe(() => {
      this.onSaveSuccess.emit();
    });
  }

  cancel(): void {
    this.onCancel.emit();
  }
}