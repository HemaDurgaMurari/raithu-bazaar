import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { AuthResponse } from "../types/auth";

interface AuthState {
  user: AuthResponse | null;
  login: (user: AuthResponse) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(persist(
  (set) => ({
    user: null,
    login: (user) => {
      localStorage.setItem("raithu_token", user.token);
      set({ user });
    },
    logout: () => {
      localStorage.removeItem("raithu_token");
      set({ user: null });
    },
  }),
  { name: "raithu_user", partialize: (state) => ({ user: state.user }) },
));
