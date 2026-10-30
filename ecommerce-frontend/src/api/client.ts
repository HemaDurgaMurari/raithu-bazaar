import axios from "axios";

export const API_URL = (import.meta.env.VITE_API_URL || "").replace(/\/$/, "");

export const api = axios.create({ baseURL: API_URL, headers: { "Content-Type": "application/json" } });

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("raithu_token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export function errorMessage(error: unknown): string {
  if (axios.isAxiosError<{ message?: string; detail?: string; error?: string }>(error)) {
    const response = error.response;
    const serverMessage = response?.data?.detail || response?.data?.message || response?.data?.error;
    if (serverMessage) return serverMessage;
    if (response?.status === 409) return "An account already exists for this email. Please sign in instead.";
    if (response?.status === 401) return "Invalid email or password.";
  }
  return error instanceof Error ? error.message : "Something went wrong. Please try again.";
}
