import { beforeEach, describe, expect, it } from 'vitest';
import { getToken, isAuthenticated, removeToken, saveToken } from '@/lib/auth';

describe('auth token storage', () => {
  beforeEach(() => {
    sessionStorage.clear();
  });

  it('saves, reads, and removes the access token', () => {
    expect(getToken()).toBeNull();
    expect(isAuthenticated()).toBe(false);

    saveToken('access-token');

    expect(getToken()).toBe('access-token');
    expect(isAuthenticated()).toBe(true);

    removeToken();

    expect(getToken()).toBeNull();
    expect(isAuthenticated()).toBe(false);
  });
});
