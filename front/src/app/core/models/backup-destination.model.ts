import { BackupType } from "./enums";

export interface BackupDestination {
  id: string;
  name: string;
  type: BackupType;
  endpoint: string;
  region?: string;
  accessKey?: string;
  secretKey?: string;
  isActive: boolean;
}

export type BackupDestinationDTO = Omit<BackupDestination, 'id' | 'accessKey' | 'secretKey'> & {
  accessKey?: string;
  secretKey?: string;
};