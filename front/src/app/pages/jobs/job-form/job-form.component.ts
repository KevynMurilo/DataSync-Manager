import { Component, Input, Output, EventEmitter, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormArray } from '@angular/forms';
import { BackupJobService } from '../../../core/services/backup-job.service';
import { BackupSourceService } from '../../../core/services/backup-source.service';
import { DestinationService } from '../../../core/services/destination.service';
import { Observable, forkJoin } from 'rxjs';
import { BackupSource } from '../../../core/models/backup-source.model';
import { BackupDestination } from '../../../core/models/backup-destination.model';
import { NotificationPolicy, ScheduleType } from '../../../core/models/enums';
import { BackupJobDTO } from '../../../core/models/backup-job.model';
import { tap } from 'rxjs/operators';
import { EmailConfigService } from '../../../core/services/email-config.service';
import { EmailConfig } from '../../../core/models/email-config.model';

@Component({
  selector: 'app-job-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './job-form.component.html'
})
export class JobFormComponent implements OnInit {
  @Input() id?: string;
  @Output() onSaveSuccess = new EventEmitter<void>();
  @Output() onCancel = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private jobService = inject(BackupJobService);
  private sourceService = inject(BackupSourceService);
  private destinationService = inject(DestinationService);
  private emailConfigService = inject(EmailConfigService);

  jobForm: FormGroup;
  isEditMode = false;
  
  sources$!: Observable<BackupSource[]>;
  emailConfigs$!: Observable<EmailConfig[]>;
  allDestinations: BackupDestination[] = [];
  
  scheduleTypes = Object.values(ScheduleType);
  notificationPolicies = Object.values(NotificationPolicy);

  constructor() {
    this.jobForm = this.fb.group({
      name: ['', Validators.required],
      sourceId: [null, Validators.required],
      scheduleType: [ScheduleType.DAILY, Validators.required],
      backupTime: ['02:00', Validators.required],
      retentionDays: [7, [Validators.required, Validators.min(1)]],
      isActive: [true, Validators.required],
      destinations: this.fb.array([], Validators.required),
      
      notificationPolicy: [NotificationPolicy.NEVER, Validators.required],
      notificationRecipients: [{value: '', disabled: true}],
      emailConfigId: [{value: null, disabled: true}]
    });

    this.jobForm.get('notificationPolicy')?.valueChanges.subscribe(policy => {
      this.updateNotificationControls(policy);
    });
  }

  ngOnInit(): void {
    this.loadInitialData();
  }

  loadInitialData(): void {
    const sources$ = this.sourceService.findAll();
    const destinations$ = this.destinationService.findAll();
    const emailConfigs$ = this.emailConfigService.findAll();

    forkJoin([sources$, destinations$, emailConfigs$]).pipe(
      tap(([sources, destinations, emailConfigs]) => {
        this.allDestinations = destinations;
        this.buildDestinationCheckboxes();
      })
    ).subscribe(([sources, destinations, emailConfigs]) => {
      this.sources$ = new Observable(sub => sub.next(sources));
      this.emailConfigs$ = new Observable(sub => sub.next(emailConfigs));
      
      if (this.id) {
        this.isEditMode = true;
        this.loadJobData();
      }
    });
  }

  loadJobData(): void {
    this.jobService.findById(this.id!).subscribe(job => {
      this.jobForm.patchValue({
        name: job.name,
        sourceId: job.source.id,
        scheduleType: job.scheduleType,
        backupTime: job.backupTime,
        retentionDays: job.retentionDays,
        isActive: job.isActive,
        notificationPolicy: job.notificationPolicy,
        notificationRecipients: job.notificationRecipients,
        emailConfigId: job.emailConfig?.id || null
      });

      this.updateNotificationControls(job.notificationPolicy);

      const destinationIds = new Set(job.destinations.map(d => d.id));
      this.destinationsFormArray.controls.forEach(control => {
        const destId = control.value.id;
        if (destinationIds.has(destId)) {
          control.patchValue({ selected: true });
        }
      });
    });
  }

  updateNotificationControls(policy: NotificationPolicy): void {
    const recipients = this.jobForm.get('notificationRecipients');
    const configId = this.jobForm.get('emailConfigId');

    if (policy === NotificationPolicy.NEVER) {
      recipients?.disable({ emitEvent: false });
      configId?.disable({ emitEvent: false });
    } else {
      recipients?.enable({ emitEvent: false });
      configId?.enable({ emitEvent: false });
    }
  }

  get destinationsFormArray() {
    return this.jobForm.get('destinations') as FormArray;
  }

  buildDestinationCheckboxes(): void {
    this.allDestinations.forEach(dest => {
      this.destinationsFormArray.push(this.fb.group({
        id: [dest.id],
        name: [dest.name],
        selected: [false]
      }));
    });
  }

  getSelectedDestinationIds(): string[] {
    return this.destinationsFormArray.value
      .filter((dest: { selected: boolean }) => dest.selected)
      .map((dest: { id: string }) => dest.id);
  }

  saveJob(): void {
    if (this.jobForm.invalid) {
      alert('Formulário inválido. Verifique os campos.');
      return;
    }

    const selectedDestIds = this.getSelectedDestinationIds();
    if (selectedDestIds.length === 0) {
      alert('Selecione ao menos um destino.');
      return;
    }
    
    const formValue = this.jobForm.getRawValue();

    if (formValue.notificationPolicy !== NotificationPolicy.NEVER && 
       (!formValue.notificationRecipients || !formValue.emailConfigId)) {
      alert('Para receber notificações, preencha os destinatários e selecione uma configuração de e-mail.');
      return;
    }

    const dto: BackupJobDTO = {
      name: formValue.name,
      sourceId: formValue.sourceId,
      scheduleType: formValue.scheduleType,
      backupTime: formValue.backupTime,
      retentionDays: formValue.retentionDays,
      isActive: formValue.isActive,
      notificationPolicy: formValue.notificationPolicy,
      notificationRecipients: formValue.notificationRecipients,
      emailConfigId: formValue.emailConfigId || undefined,
      destinationIds: selectedDestIds
    };

    const save$ = this.isEditMode
      ? this.jobService.update(this.id!, dto)
      : this.jobService.save(dto);

    save$.subscribe(() => {
      this.onSaveSuccess.emit();
    });
  }

  cancel(): void {
    this.onCancel.emit();
  }
}
