import { vi, describe, it, expect, beforeEach } from 'vitest';
import axios from 'axios';
import { login, getRewardStatus, claimReward } from '../../src/api/bloom';

vi.mock('axios');

beforeEach(() => {
  vi.clearAllMocks();
});

describe('login', () => {
  it('posts credentials and returns token', async () => {
    vi.mocked(axios.post).mockResolvedValue({ data: { token: 'jwt-abc' } });
    const token = await login('mc-token', 'Player1');
    expect(token).toBe('jwt-abc');
    expect(vi.mocked(axios.post).mock.calls[0][0]).toContain('/auth/login');
  });
});

describe('getRewardStatus', () => {
  it('calls the rewards status endpoint with bearer token', async () => {
    vi.mocked(axios.get).mockResolvedValue({ data: { claimed: false } });
    const status = await getRewardStatus('my-bloom-token');
    expect(status).toEqual({ claimed: false });
    expect(vi.mocked(axios.get).mock.calls[0][1]).toMatchObject({
      headers: { Authorization: 'Bearer my-bloom-token' },
    });
  });
});

describe('claimReward', () => {
  it('posts to the claim endpoint and returns response data', async () => {
    vi.mocked(axios.post).mockResolvedValue({ data: { success: true } });
    const result = await claimReward('my-bloom-token');
    expect(result).toEqual({ success: true });
    expect(vi.mocked(axios.post).mock.calls[0][0]).toContain('/rewards/claim');
  });
});
