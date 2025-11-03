import { DatabaseType } from "./enums";

export interface BackupSource {
  id: string;
  name: string;
  databaseType: DatabaseType;
  dbDumpToolPath: string;
  sourcePath?: string;
  dbHost?: string;
  dbPort?: number;
  dbName?: string;
  dbUser?: string;
  dbPassword?: string;
}

export type BackupSourceDTO = Omit<BackupSource, 'id'>;