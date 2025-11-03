import { BackupJob } from "./backup-job.model";
import { BackupRecord } from "./backup-record.model";

export interface DashboardStats {
  totalJobs: number;
  totalSources: number;
  totalDestinations: number;
  totalStorageUsedBytes: number;
  successRatePercentage: number;
}

export interface DailyStatusSummary {
  date: string; // ISO Date string (ex: "2025-10-31")
  successCount: number;
  failedCount: number;
}

export interface StorageUsageSummary {
  sourceName: string;
  totalBytes: number;
}

export interface DashboardData {
  stats: DashboardStats;
  dailyStatusSummary: DailyStatusSummary[];
  storageBySource: StorageUsageSummary[];
  upcomingJobs: BackupJob[];
  recentFailures: BackupRecord[];
}