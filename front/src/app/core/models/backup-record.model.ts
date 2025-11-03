import { BackupJob } from "./backup-job.model";
import { BackupStatus } from "./enums";

export interface BackupRecord {
  id: string;
  job: BackupJob;
  filename: string;
  remotePath: string;
  timestamp: string;
  sizeBytes: number;
  destinationId: string;
  status: BackupStatus;
  logSummary: string;
}