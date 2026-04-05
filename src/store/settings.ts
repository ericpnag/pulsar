import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface SettingsState {
  theme: 'dark' | 'light';
  memoryMb: number;
  javaPath: string;
  setTheme: (theme: 'dark' | 'light') => void;
  setMemoryMb: (mb: number) => void;
  setJavaPath: (path: string) => void;
}

export const useSettingsStore = create<SettingsState>()(
  persist(
    (set) => ({
      theme: 'dark',
      memoryMb: 4096,
      javaPath: '',
      setTheme: (theme) => set({ theme }),
      setMemoryMb: (memoryMb) => set({ memoryMb }),
      setJavaPath: (javaPath) => set({ javaPath }),
    }),
    {
      name: 'bloom-settings',
    }
  )
);
