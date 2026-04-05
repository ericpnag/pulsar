import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { Instance } from '../types';

interface InstanceState {
  instances: Instance[];
  selectedVersion: string;
  addInstance: (instance: Instance) => void;
  removeInstance: (id: string) => void;
  setSelectedVersion: (version: string) => void;
}

export const useInstanceStore = create<InstanceState>()(
  persist(
    (set) => ({
      instances: [],
      selectedVersion: '1.21.1',
      addInstance: (instance) =>
        set((state) => ({ instances: [...state.instances, instance] })),
      removeInstance: (id) =>
        set((state) => ({ instances: state.instances.filter((i) => i.id !== id) })),
      setSelectedVersion: (version) => set({ selectedVersion: version }),
    }),
    {
      name: 'bloom-instances',
    }
  )
);
