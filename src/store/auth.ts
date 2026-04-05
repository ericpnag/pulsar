import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { Account } from '../types';

interface AuthState {
  account: Account | null;
  setAccount: (account: Account | null) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      account: null,
      setAccount: (account) => set({ account }),
      logout: () => set({ account: null }),
    }),
    {
      name: 'bloom-auth',
    }
  )
);
