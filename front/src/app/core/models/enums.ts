export enum BackupStatus {
  SUCCESS = 'SUCCESS',
  FAILED = 'FAILED',
  IN_PROGRESS = 'IN_PROGRESS'
}

export enum BackupType {
  LOCAL_DISK = 'LOCAL_DISK',
  AMAZON_S3 = 'AMAZON_S3',
  GOOGLE_CLOUD_STORAGE = 'GOOGLE_CLOUD_STORAGE',
  FTP = 'FTP'
}

export enum DatabaseType {
  MYSQL = 'MYSQL',
  POSTGRES = 'POSTGRES',
  ORACLE = 'ORACLE',
  SQLSERVER = 'SQLSERVER',
  MARIADB = 'MARIADB',
  MONGODB = 'MONGODB',
  H2 = 'H2',
}

export enum ScheduleType {
  MANUAL = 'MANUAL',
  DAILY = 'DAILY',
  WEEKLY = 'WEEKLY'
}

export enum NotificationPolicy {
  NEVER = 'NEVER',
  ON_FAILURE = 'ON_FAILURE',
  ALWAYS = 'ALWAYS'
}