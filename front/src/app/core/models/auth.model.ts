export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
}

export interface JwtResponse {
  token: string;
  mustChangePassword: boolean;
}

export interface ChangePasswordDTO {
  email: string;
  oldPassword: string;
  newPassword: string;
}