import { describe, it, expect, beforeEach } from 'vitest';
import { useAuthStore } from '../../src/store/auth';
import { useInstanceStore } from '../../src/store/instances';

describe('useAuthStore', () => {
  beforeEach(() => {
    useAuthStore.setState({ account: null });
  });

  it('has null account in initial state', () => {
    expect(useAuthStore.getState().account).toBeNull();
  });

  it('setAccount updates the account', () => {
    const fakeAccount = { username: 'Player1', uuid: 'uuid-1', accessToken: 'tok', bloomToken: 'btok' };
    useAuthStore.getState().setAccount(fakeAccount);
    expect(useAuthStore.getState().account).toEqual(fakeAccount);
  });
});

describe('useInstanceStore', () => {
  it('has selectedVersion of 1.21.1 in initial state', () => {
    expect(useInstanceStore.getState().selectedVersion).toBe('1.21.1');
  });
});
