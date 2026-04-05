import axios from 'axios';
import type { ModResult } from '../types';

const PROXY = (import.meta.env?.VITE_BLOOM_API_URL ?? 'http://localhost:3000') + '/curseforge';

export async function searchMods(query: string, _mcVersion: string): Promise<ModResult[]> {
  const res = await axios.get(`${PROXY}/mods/search`, {
    params: { gameId: 432, searchFilter: query, modLoaderType: 4 },
  });
  return res.data.data.map((m: any) => ({
    id: String(m.id), title: m.name, description: m.summary,
    downloads: m.downloadCount, iconUrl: m.logo?.url ?? undefined, source: 'curseforge' as const,
  }));
}
