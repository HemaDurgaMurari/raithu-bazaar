export type Role = "SELLER" | "CUSTOMER";

export interface AuthRequest {
  name?: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  userId: number;
  name: string;
  email: string;
  role: Role;
  token: string;
}
