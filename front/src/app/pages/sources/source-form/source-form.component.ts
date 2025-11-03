import { Component, Input, Output, EventEmitter, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { BackupSourceService } from '../../../core/services/backup-source.service';
import { DatabaseType } from '../../../core/models/enums';
import { BackupSourceDTO } from '../../../core/models/backup-source.model';

@Component({
  selector: 'app-source-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './source-form.component.html'
})
export class SourceFormComponent implements OnInit {
  @Input() id?: string;
  @Output() onSaveSuccess = new EventEmitter<void>();
  @Output() onCancel = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private sourceService = inject(BackupSourceService);

  sourceForm: FormGroup;
  databaseTypes = Object.values(DatabaseType);
  isEditMode = false;

  isTesting = false;
  testMessage: string | null = null;
  isTestSuccess = false;

  constructor() {
    this.sourceForm = this.fb.group({
      name: ['', Validators.required],
      databaseType: [null, Validators.required],
      dbDumpToolPath: ['', Validators.required],
      sourcePath: [{ value: '', disabled: true }],
      dbHost: [{ value: 'localhost', disabled: true }],
      dbPort: [{ value: null, disabled: true }],
      dbName: [{ value: '', disabled: true }],
      dbUser: [{ value: '', disabled: true }],
      dbPassword: [{ value: '', disabled: true }],
    });

    this.sourceForm.get('databaseType')?.valueChanges.subscribe(type => {
      this.updateFormControls(type as DatabaseType);
      this.testMessage = null;
    });
  }

  ngOnInit(): void {
    if (this.id) {
      this.isEditMode = true;
      this.sourceService.findById(this.id).subscribe(source => {
        this.sourceForm.patchValue(source);
        this.updateFormControls(source.databaseType);
      });
    }
  }

  updateFormControls(type: DatabaseType | null): void {
    const sourcePath = this.sourceForm.get('sourcePath');
    const dbHost = this.sourceForm.get('dbHost');
    const dbPort = this.sourceForm.get('dbPort');
    const dbName = this.sourceForm.get('dbName');
    const dbUser = this.sourceForm.get('dbUser');
    const dbPassword = this.sourceForm.get('dbPassword');

    sourcePath?.disable({ emitEvent: false });
    dbHost?.disable({ emitEvent: false });
    dbPort?.disable({ emitEvent: false });
    dbName?.disable({ emitEvent: false });
    dbUser?.disable({ emitEvent: false });
    dbPassword?.disable({ emitEvent: false });

    if (this.isDatabaseSource(type)) {
      dbHost?.enable({ emitEvent: false });
      dbPort?.enable({ emitEvent: false });
      dbName?.enable({ emitEvent: false });
      dbUser?.enable({ emitEvent: false });
      dbPassword?.enable({ emitEvent: false });
      sourcePath?.setValue('', { emitEvent: false });
    } else if (this.isFilePathSource(type)) {
      sourcePath?.enable({ emitEvent: false });
      dbHost?.setValue('', { emitEvent: false });
      dbPort?.setValue(null, { emitEvent: false });
      dbName?.setValue('', { emitEvent: false });
      dbUser?.setValue('', { emitEvent: false });
      dbPassword?.setValue('', { emitEvent: false });
    }
  }

  get selectedDbType(): DatabaseType | null {
    return this.sourceForm.get('databaseType')?.value;
  }

  isDatabaseSource(type: DatabaseType | null): boolean {
    return type === DatabaseType.MYSQL ||
           type === DatabaseType.POSTGRES ||
           type === DatabaseType.MARIADB ||
           type === DatabaseType.ORACLE ||
           type === DatabaseType.SQLSERVER ||
           type === DatabaseType.MONGODB;
  }

  isFilePathSource(type: DatabaseType | null): boolean {
    return type === DatabaseType.H2;
  }

  onTestConnection(): void {
    if (this.sourceForm.invalid) {
      this.isTestSuccess = false;
      this.testMessage = "Formulário inválido. Preencha os campos obrigatórios para testar.";
      return;
    }

    this.isTesting = true;
    this.testMessage = null;
    this.isTestSuccess = false;
    
    const dto: BackupSourceDTO = this.sourceForm.getRawValue();
    if ((dto as any).dbPort === '') {
      (dto as any).dbPort = null;
    }

    this.sourceService.testConnection(dto).subscribe({
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

  saveSource(): void {
    if (this.sourceForm.invalid) {
      alert('Formulário inválido. Verifique os campos obrigatórios.');
      return;
    }

    const dto: BackupSourceDTO = this.sourceForm.getRawValue();
    
    if (!dto.dbPassword) {
      delete (dto as any).dbPassword;
    }
    
    if ((dto as any).dbPort === '') {
      (dto as any).dbPort = null;
    }

    const save$ = this.isEditMode
      ? this.sourceService.update(this.id!, dto)
      : this.sourceService.save(dto);

    save$.subscribe(() => {
      this.onSaveSuccess.emit();
    });
  }

  cancel(): void {
    this.onCancel.emit();
  }
}