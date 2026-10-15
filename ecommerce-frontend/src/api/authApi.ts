import type { AuthRequest, AuthResponse, Role } from "../types/auth";
import { api } from "./client";

export async function signup(request: AuthRequest, role: Role): Promise<AuthResponse> {
  const path = role === "SELLER" ? "/api/auth/signup/seller" : "/api/auth/signup/customer";
  return (await api.post<AuthResponse>(path, request)).data;
}

export async function signin(request: AuthRequest): Promise<AuthResponse> {
  return (await api.post<AuthResponse>("/api/auth/signin", request)).data;
}
