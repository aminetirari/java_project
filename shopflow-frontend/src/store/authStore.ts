import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { Role, User } from "@/lib/types";
import { clearStoredTokens, setStoredTokens } from "@/lib/api";

interface AuthState {
  token: string | null;
  refreshToken: string | null;
  user: User | null;
  roles: string[];
  setAuth: (data: {
    token: string;
    refreshToken?: string;
    user: User;
    roles: string[];
  }) => void;
  setUser: (user: User) => void;
  logout: () => void;
  hasRole: (role: Role) => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      refreshToken: null,
      user: null,
      roles: [],
      setAuth: ({ token, refreshToken, user, roles }) => {
        setStoredTokens(token, refreshToken);
        set({ token, refreshToken: refreshToken ?? null, user, roles });
      },
      setUser: (user) => set({ user }),
      logout: () => {
        clearStoredTokens();
        set({ token: null, refreshToken: null, user: null, roles: [] });
      },
      hasRole: (role) => {
        const rolesNorm = get().roles.map((r) => r.replace(/^ROLE_/, ""));
        return rolesNorm.includes(role);
      },
    }),
    { name: "shopflow-auth" }
  )
);
