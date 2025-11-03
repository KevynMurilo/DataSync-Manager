import { BackupDestination } from "./backup-destination.model";
import { BackupSource } from "./backup-source.model";
import { NotificationPolicy, ScheduleType } from "./enums";

export interface BackupJob {
  id: string;
  name: string;
  source: BackupSource;
  destinations: BackupDestination[];
  scheduleType: ScheduleType;
  backupTime: string;
  retentionDays: number;
  isActive: boolean;
  notificationPolicy: NotificationPolicy;
  notificationRecipients?: string;
  emailConfig?: { id: string, name: string };
}

export interface BackupJobDTO {
  name: string;
  sourceId: string;
  destinationIds: string[];
  scheduleType: ScheduleType;
  backupTime: string;
  retentionDays: number;
  isActive: boolean;
  notificationPolicy: NotificationPolicy;
  notificationRecipients?: string;
  emailConfigId?: string;
}