import axios from 'axios';
const BASE = import.meta.env?.VITE_BLOOM_API_URL ?? 'http://localhost:3000';

export async function login(accessToken: string, username: string): Promise<string> {
  const res = await axios.post(`${BASE}/auth/login`, { accessToken, username });
  return res.data.token;
}

export async function getRewardStatus(bloomToken: string) {
  const res = await axios.get(`${BASE}/rewards/status`, { headers: { Authorization: `Bearer ${bloomToken}` } });
  return res.data;
}

export async function claimReward(bloomToken: string) {
  const res = await axios.post(`${BASE}/rewards/claim`, {}, { headers: { Authorization: `Bearer ${bloomToken}` } });
  return res.data;
}

export async function getServerRules(): Promise<{ domain: string; restrictedModules: string[] }[]> {
  const res = await axios.get(`${BASE}/server-rules`);
  return res.data.rules;
}
