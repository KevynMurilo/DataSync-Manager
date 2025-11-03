export interface EmailConfig {
  id: string;
  name: string;
  host: string;
  port: number;
  username: string;
  password?: string;
}

export type EmailConfigDTO = Omit<EmailConfig, 'id'>;