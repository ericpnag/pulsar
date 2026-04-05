import { vi, describe, it, expect } from 'vitest';
import axios from 'axios';
import { searchMods } from '../../src/api/curseforge';

vi.mock('axios');

describe('CurseForge searchMods', () => {
  it('calls the Bloom proxy and returns mapped results', async () => {
    vi.mocked(axios.get).mockResolvedValue({ data: { data: [{ id: 1, name: 'Mod1', summary: 'A mod', downloadCount: 100, logo: null }] } });
    const results = await searchMods('test', '1.21.1');
    expect(results[0].source).toBe('curseforge');
    expect(results[0].title).toBe('Mod1');
    expect(vi.mocked(axios.get).mock.calls[0][0]).toContain('/curseforge/');
  });
});
